package com.tompee.arctictern.serializertest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_serializer_nullable", version = 1)
abstract class SerializerNullablePreference {

    @ArcticTern.ObjectProperty(IntWrapper.NullableSerializer::class)
    open var boxedInt: IntWrapper? = null
}
