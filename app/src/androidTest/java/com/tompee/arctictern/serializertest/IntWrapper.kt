package com.tompee.arctictern.serializertest

data class IntWrapper(val value: Int) {

    class Serializer : com.tompee.arctictern.nest.Serializer<IntWrapper> {

        override fun serialize(input: IntWrapper): String {
            return input.value.toString()
        }

        override fun deserialize(serializedString: String): IntWrapper {
            return Integer.parseInt(serializedString).let(::IntWrapper)
        }
    }

    class NullableSerializer : com.tompee.arctictern.nest.Serializer<IntWrapper?> {

        override fun serialize(input: IntWrapper?): String {
            return input?.value?.toString().orEmpty()
        }

        override fun deserialize(serializedString: String): IntWrapper? {
            return if (serializedString.isEmpty()) null
            else Integer.parseInt(serializedString).let(::IntWrapper)
        }
    }
}
