package com.rajpawardotin.dekhreekh.utils

import android.content.Context
import android.net.Uri
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.InputStream
import java.time.Instant
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory

object ImportEngine {

    /**
     * Parses a GPX file from the given URI and imports it into the local database.
     * @param customName Optional user-provided name for the session.
     * Returns true if successful, false otherwise.
     */
    suspend fun importGpxToDatabase(context: Context, uri: Uri, repository: SessionRepository, customName: String? = null): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    importGpxStream(inputStream, repository, customName)
                } ?: false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Internal stream importer (separated for JVM unit testing)
     * @param customName Optional user-provided name; falls back to GPX <name> tag then a generated name.
     */
    suspend fun importGpxStream(inputStream: InputStream, repository: SessionRepository, customName: String? = null): Boolean {
        return try {
            val telemetry = parseGpx(inputStream)
            if (telemetry.isEmpty()) return false

            val startTime = telemetry.first().timestamp
            val endTime = telemetry.last().timestamp
            val totalDurationSeconds = (endTime - startTime) / 1000

            var totalDistanceMeters = 0f
            for (i in 0 until telemetry.size - 1) {
                val p1 = telemetry[i]
                val p2 = telemetry[i + 1]
                totalDistanceMeters += calculateHaversineDistance(
                    p1.latitude, p1.longitude,
                    p2.latitude, p2.longitude
                )
            }

            val averagePace = if (totalDistanceMeters > 0) {
                val distanceKm = totalDistanceMeters / 1000f
                (totalDurationSeconds / distanceKm).toLong()
            } else {
                0L
            }

            val sessionId = UUID.randomUUID().toString()
            val isLow = totalDistanceMeters < 5f
            val session = WorkoutSession(
                id = sessionId,
                startTime = startTime,
                endTime = endTime,
                activityType = "RUN",
                totalDistanceMeters = totalDistanceMeters,
                totalDurationSeconds = totalDurationSeconds,
                averagePace = averagePace,
                name = customName?.takeIf { it.isNotBlank() },
                tags = if (isLow) listOf("glitch", "bogus") else emptyList(),
                isLowActivity = isLow
            )

            repository.importSession(session, telemetry)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun parseGpx(inputStream: InputStream): List<TelemetryData> {
        val points = mutableListOf<TelemetryData>()
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(inputStream)
        doc.documentElement.normalize()

        val trkptList = doc.getElementsByTagName("trkpt")
        for (i in 0 until trkptList.length) {
            val node = trkptList.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                val lat = element.getAttribute("lat").toDoubleOrNull()
                val lon = element.getAttribute("lon").toDoubleOrNull()

                var ele: Double? = null
                var time: Long? = null

                val children = element.childNodes
                for (j in 0 until children.length) {
                    val child = children.item(j)
                    if (child.nodeType == Node.ELEMENT_NODE) {
                        val childElement = child as Element
                        when (childElement.tagName.lowercase()) {
                            "ele" -> ele = childElement.textContent.trim().toDoubleOrNull()
                            "time" -> {
                                val timeStr = childElement.textContent.trim()
                                time = try {
                                    Instant.parse(timeStr).toEpochMilli()
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }
                    }
                }

                if (lat != null && lon != null) {
                    points.add(
                        TelemetryData(
                            latitude = lat,
                            longitude = lon,
                            altitude = ele ?: 0.0,
                            accuracy = 3.0f, // default accuracy for imported tracks
                            speed = 0f, // default speed
                            timestamp = time ?: System.currentTimeMillis()
                        )
                    )
                }
            }
        }
        return points
    }

    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val earthRadius = 6371000.0 // in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }
}
