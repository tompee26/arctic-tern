package com.tompee.arctictern.nullableserializertest

import com.tompee.arctictern.nest.NullableSerializer

data class StringWrapper(val value: String) {

    class Serializer : NullableSerializer<StringWrapper> {

        override fun serialize(input: StringWrapper): String? {
            return if (input.value.isEmpty()) null
            else input.value
        }

        override fun deserialize(serializedString: String?): StringWrapper {
            return serializedString.orEmpty().let(::StringWrapper)
        }
    }

    class NullSerializer : NullableSerializer<StringWrapper?> {

        override fun serialize(input: StringWrapper?): String? {
            return input?.value
        }

        override fun deserialize(serializedString: String?): StringWrapper? {
            return serializedString?.let(::StringWrapper)
        }
    }
}
