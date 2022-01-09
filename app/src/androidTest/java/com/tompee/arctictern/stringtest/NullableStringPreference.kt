package com.tompee.arctictern.stringtest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_nullable_string", version = 1)
abstract class NullableStringPreference {

    @ArcticTern.Property
    open var name: String? = null
}
