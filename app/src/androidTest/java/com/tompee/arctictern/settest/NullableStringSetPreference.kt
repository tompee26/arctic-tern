package com.tompee.arctictern.settest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_nullable_string_set", version = 1)
abstract class NullableStringSetPreference {

    @ArcticTern.Property
    open var nameSet: Set<String?> = setOf(null)
}
