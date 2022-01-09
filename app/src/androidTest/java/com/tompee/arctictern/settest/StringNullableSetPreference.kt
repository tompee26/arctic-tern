package com.tompee.arctictern.settest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_string_nullable_set", version = 1)
abstract class StringNullableSetPreference {

    @ArcticTern.Property
    open var nameSet: Set<String>? = null
}
