package com.tompee.arctictern.nest

import android.content.SharedPreferences

/**
 * An interface definition of class that will be invoked in the event of migration
 * Implementation classes must be declared as either an object or a no-arg constructor class.
 */
fun interface Migration {

    /**
     * Called when migration needs to happen. This will be executed when a migration needs to happen
     * from the previous version to the target [version]. A [SharedPreferences] instance will be
     * provided to give full access to the backing persistence layer.
     *
     * @param version the target version
     * @param sharedPreferences the [SharedPreferences] instance that you can use to manipulate the
     *                          backing persistence layer
     */
    fun onMigrate(version: Int, sharedPreferences: SharedPreferences)
}
