package com.tompee.arctictern.nest

/**
 * A class annotated with this will have a generated functions that can be used to
 * support data persistence powered by Kotlin Flow and SharedPreferences.
 *
 * This can only be used on abstract classes.
 *
 * @property preferenceFile the preference filename
 * @property version version code
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ArcticTern(val preferenceFile: String, val version: Int) {

    /**
     * Denotes a var property that can be used to get and set the value
     * Properties must be open for override
     *
     * @property key the shared preference key. If not set, will autogenerate using
     *               the property name prepended with "key", e.g. "key_timestamp".
     *               Keys are important and must not change once created, otherwise, the pointer
     *               to the data might get lost.
     */
    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Property(val key: String = "arcticterndefault")
}
