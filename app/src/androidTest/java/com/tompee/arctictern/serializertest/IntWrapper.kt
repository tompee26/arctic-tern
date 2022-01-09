package com.tompee.arctictern.serializertest

import com.tompee.arctictern.nest.Serializer

data class IntWrapper(val value: Int) {

    class MySerializer : Serializer<IntWrapper> {

        override fun serialize(input: IntWrapper): String {
            return input.value.toString()
        }

        override fun deserialize(serializedString: String): IntWrapper {
            return Integer.parseInt(serializedString).let(::IntWrapper)
        }
    }
}
