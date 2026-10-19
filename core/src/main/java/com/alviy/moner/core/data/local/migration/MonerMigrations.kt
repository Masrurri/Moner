package com.alviy.moner.core.data.local.migration

import androidx.room.migration.Migration

/**
 * Explicit schema migrations for [com.alviy.moner.core.data.local.MonerDatabase].
 *
 * Version 13 is the first version with an exported schema (core/schemas). Every schema change
 * from here on MUST:
 *  1. bump `version` in `@Database`,
 *  2. add a `Migration(old, new)` below and append it to [ALL],
 *  3. commit the newly exported `schemas/<version>.json`,
 *  4. add a case to `MonerMigrationTest`.
 *
 * Upgrades never fall back to a destructive rebuild, so a missing migration crashes on open
 * instead of silently wiping users' expenses.
 *
 * Example:
 * ```
 * val MIGRATION_13_14 = object : Migration(13, 14) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE expense_transaction ADD COLUMN merchant TEXT")
 *     }
 * }
 * ```
 */
object MonerMigrations {

    /** First version with an exported schema; older (pre-release) installs are rebuilt. */
    const val BASELINE_VERSION = 13

    val ALL: Array<Migration> = arrayOf()

    /** Pre-baseline versions have no schema on record, so they cannot be migrated. */
    val UNMIGRATABLE_VERSIONS: IntArray = (1 until BASELINE_VERSION).toList().toIntArray()
}
