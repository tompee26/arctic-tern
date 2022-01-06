package com.tompee.arctictern.booleantest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_boolean", version = 1)
abstract class BooleanPreference {

    @ArcticTern.Property(key = "isSuccessful")
    open var isSuccessful = false
}
