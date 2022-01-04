package com.tompee.arctictern.nest

import kotlin.reflect.KClass

/**
 * Default key identifier
 */
const val DEFAULT_KEY = "arcticterndefault"

/**
 * A class annotated with this will have a generated functions that can be used to
 * support data persistence powered by Kotlin Flow and SharedPreferences.
 *
 * This can only be used on abstract classes.
 *
 * An implementation of the target class will be generated reimplementing the default properties.
 * To take advantage of the custom properties and functions such as flow and delete, use the
 * generated type instead of the target abstract class.
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
     * @property withFlow when true, a flow version of a property will be generated that
     *                    allows you to observe the changes in value. The property name will be
     *                    based on the target property appended with flow, e.g. "preferenceFlow".
     * @property withDelete when true, a delete function of a property will be generated. The
     *                      property name will be based on the target property prepended with delete,
     *                      and camel case e.g. "deletePreference".
     */
    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Property(
        val key: String = DEFAULT_KEY,
        val withFlow: Boolean = true,
        val withDelete: Boolean = true
    )

    /**
     * Denotes a var property that can be used to get and set the value of an object. This
     * requires a serializer implementation. Properties must be open for override
     *
     * @property serializer serializer implementation. Must be declared as an object or a class
     *                      with no-arg constructor.
     * @property key the shared preference key. If not set, will autogenerate using
     *               the property name prepended with "key", e.g. "key_timestamp".
     *               Keys are important and must not change once created, otherwise, the pointer
     *               to the data might get lost.
     * @property withFlow when true, a flow version of a property will be generated that
     *                    allows you to observe the changes in value. The property name will be
     *                    based on the target property appended with flow, e.g. "preferenceFlow".
     * @property withDelete when true, a delete function of a property will be generated. The
     *                      property name will be based on the target property prepended with delete,
     *                      and camel case e.g. "deletePreference".
     */
    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ObjectProperty(
        val serializer: KClass<out Serializer<*>>,
        val key: String = DEFAULT_KEY,
        val withFlow: Boolean = true,
        val withDelete: Boolean = true
    )
}
