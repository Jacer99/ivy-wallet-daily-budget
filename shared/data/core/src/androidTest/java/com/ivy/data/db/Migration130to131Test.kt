package com.ivy.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.ivy.data.db.migration.Migration130to131_DynamicBudgetConfig
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.util.UUID

class Migration130to131Test {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        IvyRoomDatabase::class.java,
        listOf(IvyRoomDatabase.DeleteSEMigration()),
        FrameworkSQLiteOpenHelperFactory()
    )

    private val migration = Migration130to131_DynamicBudgetConfig()

    @Test
    fun existingRowsSurvive() = migrationTestCase(
        dataBeforeMigration = {
            insertAccount("Acc 1")
            insertCategory("Cat 1")
        },
        dataAfterMigration = {
            query("SELECT name FROM accounts").apply {
                moveToFirst() shouldBe true
                getString(0) shouldBe "Acc 1"
                close()
            }
            query("SELECT name FROM categories").apply {
                moveToFirst() shouldBe true
                getString(0) shouldBe "Cat 1"
                close()
            }
        }
    )

    @Test
    fun newTableIsEmptyAfterMigration() = migrationTestCase(
        dataBeforeMigration = {},
        dataAfterMigration = {
            query("SELECT * FROM dynamic_budget_config").apply {
                moveToFirst() shouldBe false
                close()
            }
        }
    )

    @Test
    fun canInsertAndReadBackConfigAfterMigration() = migrationTestCase(
        dataBeforeMigration = {},
        dataAfterMigration = {
            val id = UUID.randomUUID().toString()
            val now = Instant.now().toEpochMilli()
            execSQL(
                "INSERT INTO dynamic_budget_config (id, periodTypeTag, salaryPayday, customStartEpochDay, customEndEpochDay, budgetLimitMinorUnits, currencyCode, includedAccountIdsSerialized, includedCategoryIdsSerialized, includeIncomeInBudget, dateTime, isSynced, isDeleted) VALUES (?, 'WEEKLY', NULL, NULL, NULL, 1000, 'USD', NULL, NULL, 0, ?, 1, 0)",
                arrayOf(id, now)
            )

            query("SELECT * FROM dynamic_budget_config WHERE id = ?", arrayOf(id)).apply {
                moveToFirst() shouldBe true
                getString(getColumnIndexOrThrow("periodTypeTag")) shouldBe "WEEKLY"
                getLong(getColumnIndexOrThrow("budgetLimitMinorUnits")) shouldBe 1000L
                getString(getColumnIndexOrThrow("currencyCode")) shouldBe "USD"
                close()
            }
        }
    )

    private fun SupportSQLiteDatabase.insertAccount(name: String) {
        execSQL(
            "INSERT INTO accounts (name, currency, color, icon, orderNum, includeInBalance, isSynced, isDeleted, id) VALUES (?, 'USD', 0, '', 1.0, 1, 1, 0, ?)",
            arrayOf(name, UUID.randomUUID().toString())
        )
    }

    private fun SupportSQLiteDatabase.insertCategory(name: String) {
        execSQL(
            "INSERT INTO categories (name, color, icon, orderNum, isSynced, isDeleted, id) VALUES (?, 0, '', 1.0, 1, 0, ?)",
            arrayOf(name, UUID.randomUUID().toString())
        )
    }

    private fun migrationTestCase(
        dataBeforeMigration: SupportSQLiteDatabase.() -> Unit,
        dataAfterMigration: SupportSQLiteDatabase.() -> Unit,
    ) {
        helper.createDatabase(TestDb, 130).apply {
            dataBeforeMigration()
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TestDb,
            131,
            true,
            migration,
        )

        db.dataAfterMigration()
        db.close()
    }

    companion object {
        private const val TestDb = "migration-test"
    }
}