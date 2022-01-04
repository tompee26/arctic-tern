package com.tompee.arctictern.nest

/**
 * An interface that must be implement to allow serialization and deserialization
 * of objects
 */
interface Serializer<T> {

    /**
     * Converts the object into a string
     */
    fun serialize(input: T): String

    /**
     * Converts the string into
     */
    fun deserialize(serializedString: String): T
}
