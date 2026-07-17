package com.rajpawardotin.dekhreekh.data.local

import androidx.room.TypeConverter

/**
 * Room TypeConverter for List<String> tags ↔ comma-separated String.
 * e.g. listOf("morning", "trail") ↔ "morning,trail"
 */
class TagsConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return if (value.isBlank()) emptyList()
        else value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromList(tags: List<String>): String {
        return tags.joinToString(",")
    }
}
