package com.tompee.arctictern.serializertest

data class IntWrapper(val value: Int) {

    class Serializer : com.tompee.arctictern.nest.Serializer<IntWrapper> {

        override fun serialize(input: IntWrapper): String {
            return input.value.toString()
        }

        override fun deserialize(input: String): IntWrapper {
            return Integer.parseInt(input).let(::IntWrapper)
        }
    }

    class NullableSerializer : com.tompee.arctictern.nest.Serializer<IntWrapper?> {

        override fun serialize(input: IntWrapper?): String {
            return input?.value?.toString().orEmpty()
        }

        override fun deserialize(input: String): IntWrapper? {
            return if (input.isEmpty()) null
            else Integer.parseInt(input).let(::IntWrapper)
        }
    }
}
