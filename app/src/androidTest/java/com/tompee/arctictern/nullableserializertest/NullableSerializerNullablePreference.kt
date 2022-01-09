package com.tompee.arctictern.nullableserializertest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_nullable_serializer_nullable", version = 1)
abstract class NullableSerializerNullablePreference {

    @ArcticTern.NullableObjectProperty(StringWrapper.NullSerializer::class)
    open var boxedString: StringWrapper? = null
}
