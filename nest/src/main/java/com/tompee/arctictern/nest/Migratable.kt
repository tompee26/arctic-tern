package com.tompee.arctictern.nest

/**
 * Marks a class as migration-capable
 */
interface Migratable {

    /**
     * Returns true if this is already migrated
     */
    val isUpdated: Boolean

    /**
     * Setup and initialize
     */
    fun initialize()

    /**
     * Start migration
     */
    fun migrate()
}
