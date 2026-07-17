package com.rajpawardotin.dekhreekh.utils

import android.content.Context
import android.net.Uri
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.time.Instant
import java.time.format.DateTimeFormatter

object ExportEngine {

    /**
     * Extracts all telemetry from Room and writes it to a standard .gpx file.
     * Returns true if successful, false if it failed or the database was empty.
     */
    suspend fun exportDatabaseToGpx(context: Context, uri: Uri, sessionRepository: SessionRepository): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val points = sessionRepository.getAllTelemetry()
                if (points.isEmpty()) return@withContext false
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writeGpxPoints(writer, "Dekhreekh Flagship Export", points)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Exports a single session's telemetry to GPX, using the session name as the track name.
     */
    suspend fun exportSessionToGpx(
        context: Context,
        uri: Uri,
        sessionId: String,
        sessionName: String?,
        sessionRepository: SessionRepository
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val points = sessionRepository.getTelemetryForSessionOnce(sessionId)
                if (points.isEmpty()) return@withContext false
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writeGpxPoints(writer, sessionName ?: "Session $sessionId", points)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun writeGpxPoints(writer: OutputStreamWriter, trackName: String, points: List<com.rajpawardotin.dekhreekh.domain.models.TelemetryData>) {
        val timeFormatter = DateTimeFormatter.ISO_INSTANT
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        writer.write("<gpx version=\"1.1\" creator=\"Dekhreekh Telemetry Engine\">\n")
        writer.write("  <trk>\n")
        writer.write("    <name>$trackName</name>\n")
        writer.write("    <trkseg>\n")
        points.forEach { point ->
            val timeString = timeFormatter.format(Instant.ofEpochMilli(point.timestamp))
            writer.write("      <trkpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">\n")
            writer.write("        <ele>${point.altitude}</ele>\n")
            writer.write("        <time>${timeString}</time>\n")
            writer.write("      </trkpt>\n")
        }
        writer.write("    </trkseg>\n")
        writer.write("  </trk>\n")
        writer.write("</gpx>\n")
    }
}
