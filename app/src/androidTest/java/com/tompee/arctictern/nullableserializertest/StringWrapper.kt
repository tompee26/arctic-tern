package com.tompee.arctictern.nullableserializertest

import com.tompee.arctictern.nest.NullableSerializer

data class StringWrapper(val value: String) {

    class Serializer : NullableSerializer<StringWrapper> {

        override fun serialize(input: StringWrapper): String? {
            return if (input.value.isEmpty()) {
                null
            } else {
                input.value
            }
        }

        override fun deserialize(input: String?): StringWrapper {
            return input.orEmpty().let(::StringWrapper)
        }
    }

    class NullSerializer : NullableSerializer<StringWrapper?> {

        override fun serialize(input: StringWrapper?): String? {
            return input?.value
        }

        override fun deserialize(input: String?): StringWrapper? {
            return input?.let(::StringWrapper)
        }
    }
}
