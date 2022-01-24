package com.tompee.arctictern.nest

import android.content.Context

/**
 * Marks a class as migration-capable
 */
interface Migratable {

    /**
     * Returns true if this is already migrated
     */
    fun isUpdated(context: Context): Boolean

    /**
     * Setup and initialize
     */
    fun initialize(context: Context)

    /**
     * Start migration
     */
    fun migrate(context: Context)
}
