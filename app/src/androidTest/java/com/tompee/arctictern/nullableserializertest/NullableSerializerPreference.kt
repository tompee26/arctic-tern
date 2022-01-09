package com.tompee.arctictern.nullableserializertest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_nullable_serializer", version = 1)
abstract class NullableSerializerPreference {

    @ArcticTern.NullableObjectProperty(StringWrapper.Serializer::class)
    open var boxedString = StringWrapper("")
}
