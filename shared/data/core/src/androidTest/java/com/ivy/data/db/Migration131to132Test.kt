package com.ivy.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.ivy.data.db.migration.Migration131to132_Reservations
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.util.UUID

class Migration131to132Test {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        IvyRoomDatabase::class.java,
        listOf(IvyRoomDatabase.DeleteSEMigration()),
        FrameworkSQLiteOpenHelperFactory()
    )

    private val migration = Migration131to132_Reservations()

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
            query("SELECT * FROM reservations").apply {
                moveToFirst() shouldBe false
                close()
            }
        }
    )

    @Test
    fun canInsertAndReadBackReservationAfterMigration() = migrationTestCase(
        dataBeforeMigration = {},
        dataAfterMigration = {
            val id = UUID.randomUUID().toString()
            val now = Instant.now().toEpochMilli()
            execSQL(
                "INSERT INTO reservations (id, name, amountMinorUnits, currencyCode, dueEpochDay, categoryId, accountId, linkedTransactionId, dateTime, isSynced, isDeleted) VALUES (?, 'Test reservation', 5000, 'TND', NULL, NULL, NULL, NULL, ?, 1, 0)",
                arrayOf(id, now)
            )

            query("SELECT * FROM reservations WHERE id = ?", arrayOf(id)).apply {
                moveToFirst() shouldBe true
                getLong(getColumnIndexOrThrow("amountMinorUnits")) shouldBe 5000L
                getString(getColumnIndexOrThrow("currencyCode")) shouldBe "TND"
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
        helper.createDatabase(TestDb, 131).apply {
            dataBeforeMigration()
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TestDb,
            132,
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
