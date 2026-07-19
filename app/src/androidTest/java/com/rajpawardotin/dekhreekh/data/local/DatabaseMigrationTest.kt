package com.rajpawardotin.dekhreekh.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        DekhreekhDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        // 1. Create database in version 2
        var db = helper.createDatabase(TEST_DB, 2)

        // 2. Insert data using SQL queries (since version 2 entity classes don't match latest)
        val values = ContentValues().apply {
            put("id", "session-v2-test")
            put("startTime", 123456789L)
            put("endTime", 123456790L)
            put("activityType", "RUN")
            put("totalDistanceMeters", 1500.5f)
            put("totalDurationSeconds", 300L)
            put("averagePace", 200L)
        }
        db.insert("workout_sessions", SQLiteDatabase.CONFLICT_REPLACE, values)
        db.close()

        // 3. Migrate database to version 3
        // Room will run the auto-migration from version 2 to 3
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true)

        // 4. Verify that data was preserved and default values were added correctly
        val cursor = db.query("SELECT * FROM workout_sessions WHERE id = 'session-v2-test'")
        assertNotNull(cursor)
        assertEquals(true, cursor.moveToFirst())

        // Verify old fields
        assertEquals(123456789L, cursor.getLong(cursor.getColumnIndexOrThrow("startTime")))
        assertEquals(1500.5f, cursor.getFloat(cursor.getColumnIndexOrThrow("totalDistanceMeters")), 0.01f)

        // Verify default values for new fields in v3
        assertEquals("", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEquals("", cursor.getString(cursor.getColumnIndexOrThrow("tags")))
        assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("isLowActivity")))

        cursor.close()
    }
}
