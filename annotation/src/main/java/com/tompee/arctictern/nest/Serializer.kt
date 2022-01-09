package com.tompee.arctictern.nest

/**
 * An interface that must be implemented to allow serialization to non-nullable strings and
 * deserialization using non-nullable inputs
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
