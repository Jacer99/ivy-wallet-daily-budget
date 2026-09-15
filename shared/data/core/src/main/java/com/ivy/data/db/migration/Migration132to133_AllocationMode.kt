package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration132to133_AllocationMode : Migration(132, 133) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE `transactions` ADD COLUMN `allocationMode` TEXT NOT NULL DEFAULT 'TODAY'"
        )
    }
}