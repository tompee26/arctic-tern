package com.tompee.arctictern.settest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_nullable_string_nullable_set", version = 1)
abstract class NullableStringNullableSetPreference {

    @ArcticTern.Property
    open var nameSet: Set<String?>? = null
}
