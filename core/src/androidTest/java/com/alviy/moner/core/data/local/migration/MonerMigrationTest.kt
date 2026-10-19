package com.alviy.moner.core.data.local.migration

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.alviy.moner.core.data.local.MonerDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Guards against data loss on app updates: migrates from the baseline schema through every
 * registered migration, validates the result against the exported schema, and checks that the
 * user's rows survive opening the real database.
 */
@RunWith(AndroidJUnit4::class)
class MonerMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MonerDatabase::class.java,
    )

    @Test
    fun migrateBaselineToLatest_keepsData() {
        helper.createDatabase(TEST_DB, MonerMigrations.BASELINE_VERSION).use { db ->
            db.execSQL(
                "INSERT INTO expense_category (categoryId, categoryName, monthYear, dateCreated) " +
                    "VALUES (1, 'Food', '2026-10', '2026-10-01')"
            )
            db.execSQL(
                "INSERT INTO expense_transaction (transactionId, categoryId, date, `desc`, source, amount, note) " +
                    "VALUES (1, 1, '2026-10-02', 'Lunch', 'Cash', '25000', NULL)"
            )
        }

        val room = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MonerDatabase::class.java,
            TEST_DB,
        ).addMigrations(*MonerMigrations.ALL).build()

        try {
            room.openHelper.writableDatabase.query("SELECT amount FROM expense_transaction").use { c ->
                assertEquals(1, c.count)
                c.moveToFirst()
                assertEquals("25000", c.getString(0))
            }
            // Re-validate the migrated schema against the latest exported JSON.
            val latest = room.openHelper.readableDatabase.version
            room.close()
            helper.runMigrationsAndValidate(TEST_DB, latest, true, *MonerMigrations.ALL).close()
        } finally {
            if (room.isOpen) room.close()
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
