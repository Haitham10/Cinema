package com.example.cinema.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE favourite_movies
            ADD COLUMN localPosterPath TEXT
            """.trimIndent()
        )

        db.execSQL(
            """
            ALTER TABLE favourite_movies
            ADD COLUMN localBackdropPath TEXT
            """.trimIndent()
        )
    }
}