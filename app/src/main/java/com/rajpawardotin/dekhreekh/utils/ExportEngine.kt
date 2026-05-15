package com.rajpawardotin.dekhreekh.utils

import android.content.Context
import android.net.Uri
import com.rajpawardotin.dekhreekh.data.DekhreekhDatabase
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
    suspend fun exportDatabaseToGpx(context: Context, uri: Uri, db: DekhreekhDatabase): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val points = db.telemetryDao().getAllPoints()
                if (points.isEmpty()) return@withContext false

                // Open the file stream provided by the Android Storage Access Framework
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        
                        // GPX Standard XML Header
                        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                        writer.write("<gpx version=\"1.1\" creator=\"Dekhreekh Telemetry Engine\">\n")
                        writer.write("  <trk>\n")
                        writer.write("    <name>Dekhreekh Flagship Export</name>\n")
                        writer.write("    <trkseg>\n")

                        // GPX requires strictly formatted ISO-8601 timestamps (e.g., 2026-05-14T15:30:00Z)
                        val timeFormatter = DateTimeFormatter.ISO_INSTANT

                        // Stream the points into XML nodes
                        points.forEach { point ->
                            val timeString = timeFormatter.format(Instant.ofEpochMilli(point.timestamp))
                            
                            writer.write("      <trkpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">\n")
                            writer.write("        <ele>${point.altitude}</ele>\n")
                            writer.write("        <time>${timeString}</time>\n")
                            // Optional: You can inject your custom speed/accuracy data as extensions here if you want
                            writer.write("      </trkpt>\n")
                        }

                        // Close the XML structure
                        writer.write("    </trkseg>\n")
                        writer.write("  </trk>\n")
                        writer.write("</gpx>\n")
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
