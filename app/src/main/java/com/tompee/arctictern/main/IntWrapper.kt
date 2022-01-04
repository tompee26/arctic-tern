package com.tompee.arctictern.main

import com.tompee.arctictern.nest.Serializer

internal data class IntWrapper(val intData: Int) {

    class DataSerializer : Serializer<IntWrapper> {

        override fun serialize(input: IntWrapper): String {
            return input.intData.toString()
        }

        override fun deserialize(serializedString: String): IntWrapper {
            return IntWrapper(Integer.parseInt(serializedString))
        }
    }
}
