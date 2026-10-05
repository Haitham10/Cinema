package com.example.cinema.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cinema.data.local.migration.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CinemaDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        CinemaDatabase::class.java
    )

    @Test
    fun migrate1To2_preservesMovieAndAddsNullableImagePaths() {
        val databaseName = "cinema_migration_test"

        helper.createDatabase(databaseName, 1).use { database ->
            database.execSQL(
                """
                INSERT INTO favourite_movies (
                    id,
                    title,
                    rating,
                    overview,
                    posterUrl,
                    backdropUrl,
                    releaseDate,
                    genreIdsJson,
                    savedAt
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any>(
                    42,
                    "Saved movie",
                    8.5,
                    "Saved overview",
                    "https://example.com/poster.jpg",
                    "https://example.com/backdrop.jpg",
                    "2026-01-15",
                    "[28,12]",
                    1_000L
                )
            )
        }

        helper.runMigrationsAndValidate(
            databaseName,
            2,
            true,
            MIGRATION_1_2
        ).use { database ->
            database.query(
                "SELECT * FROM favourite_movies"
            ).use { cursor ->
                assertEquals(1, cursor.count)
                assertTrue(cursor.moveToFirst())

                assertEquals(
                    42,
                    cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                )
                assertEquals(
                    "Saved movie",
                    cursor.getString(cursor.getColumnIndexOrThrow("title"))
                )
                assertEquals(
                    8.5,
                    cursor.getDouble(cursor.getColumnIndexOrThrow("rating")),
                    0.0001
                )
                assertEquals(
                    "Saved overview",
                    cursor.getString(cursor.getColumnIndexOrThrow("overview"))
                )
                assertEquals(
                    "https://example.com/poster.jpg",
                    cursor.getString(cursor.getColumnIndexOrThrow("posterUrl"))
                )
                assertEquals(
                    "https://example.com/backdrop.jpg",
                    cursor.getString(cursor.getColumnIndexOrThrow("backdropUrl"))
                )
                assertEquals(
                    "2026-01-15",
                    cursor.getString(cursor.getColumnIndexOrThrow("releaseDate"))
                )
                assertEquals(
                    "[28,12]",
                    cursor.getString(cursor.getColumnIndexOrThrow("genreIdsJson"))
                )
                assertEquals(
                    1_000L,
                    cursor.getLong(cursor.getColumnIndexOrThrow("savedAt"))
                )

                assertTrue(
                    cursor.isNull(
                        cursor.getColumnIndexOrThrow("localPosterPath")
                    )
                )
                assertTrue(
                    cursor.isNull(
                        cursor.getColumnIndexOrThrow("localBackdropPath")
                    )
                )
            }
        }
    }
}