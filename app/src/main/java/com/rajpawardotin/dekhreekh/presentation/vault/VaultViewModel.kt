package com.rajpawardotin.dekhreekh.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ── Sort order ────────────────────────────────────────────────────────────────

enum class SortOrder(val label: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    DISTANCE_DESC("Longest First"),
    DURATION_DESC("Longest Duration")
}

// ── Grouped list item ─────────────────────────────────────────────────────────

sealed interface VaultListItem {
    data class Header(val dateLabel: String) : VaultListItem
    data class SessionItem(val session: WorkoutSession) : VaultListItem
}

// ── UI state ──────────────────────────────────────────────────────────────────

sealed interface VaultState {
    data object Empty : VaultState
    data class HistoryLoaded(
        val items: List<VaultListItem>,
        val allTags: Set<String>
    ) : VaultState
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class VaultViewModel(
    val sessionRepository: SessionRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _activeTagFilter = MutableStateFlow<Set<String>>(emptySet())
    val activeTagFilter: StateFlow<Set<String>> = _activeTagFilter.asStateFlow()

    private val _hideLowActivity = MutableStateFlow(false)
    val hideLowActivity: StateFlow<Boolean> = _hideLowActivity.asStateFlow()

    private val dateGroupFormatter = DateTimeFormatter.ofPattern("EEE, MMM dd yyyy")

    val tagUsageCounts: StateFlow<Map<String, Int>> = sessionRepository.getAllSessions()
        .map { sessions ->
            val counts = mutableMapOf<String, Int>()
            sessions.flatMap { it.tags }.forEach { tag ->
                counts[tag] = (counts[tag] ?: 0) + 1
            }
            counts
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val uiState: StateFlow<VaultState> = combine(
        sessionRepository.getAllSessions(),
        _sortOrder,
        _activeTagFilter,
        _hideLowActivity
    ) { sessions, sort, tagFilter, hideLow ->

        // 1. Filter low activity if toggle is on
        val afterLow = if (hideLow) sessions.filter { !it.isLowActivity } else sessions

        // 2. Filter by active tag set
        val afterTags = if (tagFilter.isEmpty()) afterLow
        else afterLow.filter { session ->
            session.tags.any { tag ->
                tagFilter.any { filter -> filter.equals(tag, ignoreCase = true) }
            }
        }

        // 3. Sort
        val sorted = when (sort) {
            SortOrder.DATE_DESC    -> afterTags.sortedByDescending { it.startTime }
            SortOrder.DATE_ASC     -> afterTags.sortedBy { it.startTime }
            SortOrder.DISTANCE_DESC -> afterTags.sortedByDescending { it.totalDistanceMeters }
            SortOrder.DURATION_DESC -> afterTags.sortedByDescending { it.totalDurationSeconds }
        }

        if (sorted.isEmpty()) return@combine VaultState.Empty

        // 4. Group by date (for DATE sorts) or keep flat (for metric sorts)
        val items: List<VaultListItem> = when (sort) {
            SortOrder.DATE_DESC, SortOrder.DATE_ASC -> {
                val grouped = sorted.groupBy { session ->
                    Instant.ofEpochMilli(session.startTime)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
                buildList {
                    grouped.entries.forEach { (date, sessionsForDate) ->
                        add(VaultListItem.Header(date.format(dateGroupFormatter)))
                        sessionsForDate.forEach { add(VaultListItem.SessionItem(it)) }
                    }
                }
            }
            else -> sorted.map { VaultListItem.SessionItem(it) }
        }

        // 5. Collect all known tags for the filter chip bar
        val allTags = sessions.flatMap { it.tags }.toSet()

        VaultState.HistoryLoaded(items = items, allTags = allTags)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaultState.Empty
    )

    // ── User actions ──────────────────────────────────────────────────────────

    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }

    fun toggleTagFilter(tag: String) {
        _activeTagFilter.update { current ->
            if (tag in current) current - tag else current + tag
        }
    }

    fun clearTagFilter() { _activeTagFilter.value = emptySet() }

    fun setHideLowActivity(hide: Boolean) { _hideLowActivity.value = hide }

    fun renameSession(sessionId: String, newName: String, tags: List<String>) {
        viewModelScope.launch {
            sessionRepository.updateSessionMeta(sessionId, newName.takeIf { it.isNotBlank() }, tags)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }

    fun renameTagGlobally(oldTag: String, newTag: String) {
        viewModelScope.launch {
            sessionRepository.renameTagGlobally(oldTag, newTag)
        }
    }

    fun deleteTagGlobally(tag: String) {
        viewModelScope.launch {
            sessionRepository.deleteTagGlobally(tag)
        }
    }
}
