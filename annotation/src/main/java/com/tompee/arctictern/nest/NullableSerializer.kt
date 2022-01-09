package com.tompee.arctictern.nest

/**
 * An interface that must be implemented to allow serialization to nullable strings and
 * deserialization using nullable inputs
 */
interface NullableSerializer<T> {

    /**
     * Converts the object into a string
     */
    fun serialize(input: T): String?

    /**
     * Converts the string into an instance of [T]
     */
    fun deserialize(input: String?): T
}
