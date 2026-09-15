package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration131to132_Reservations : Migration(131, 132) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `reservations` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `amountMinorUnits` INTEGER NOT NULL, `currencyCode` TEXT NOT NULL, `dueEpochDay` INTEGER, `categoryId` TEXT, `accountId` TEXT, `linkedTransactionId` TEXT, `dateTime` INTEGER NOT NULL, `isSynced` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
    }
}
