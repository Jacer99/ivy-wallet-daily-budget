package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration130to131_DynamicBudgetConfig : Migration(130, 131) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `dynamic_budget_config` (`id` TEXT NOT NULL, `periodTypeTag` TEXT NOT NULL, `salaryPayday` INTEGER, `customStartEpochDay` INTEGER, `customEndEpochDay` INTEGER, `budgetLimitMinorUnits` INTEGER NOT NULL, `currencyCode` TEXT NOT NULL, `includedAccountIdsSerialized` TEXT, `includedCategoryIdsSerialized` TEXT, `includeIncomeInBudget` INTEGER NOT NULL, `dateTime` INTEGER NOT NULL, `isSynced` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
    }
}