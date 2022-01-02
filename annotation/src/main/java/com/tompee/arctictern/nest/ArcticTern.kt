package com.tompee.arctictern.nest

/**
 * A class annotated with this will have a generated functions that can be used to
 * support data persistence powered by Kotlin Flow and SharedPreferences.
 *
 * This can only be used on interfaces.
 *
 * @property filename the preference filename
 * @property version version code
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ArcticTern(val filename: String, val version: Int) {

    /**
     * Denotes a property that will be used to generate setter, getter and observer methods
     */
    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Property
}
