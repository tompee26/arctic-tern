package com.tompee.arctictern.serializertest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_serializer", version = 1)
abstract class SerializerPreference {

    @ArcticTern.ObjectProperty(IntWrapper.Serializer::class)
    open var boxedInt = IntWrapper(20)
}
