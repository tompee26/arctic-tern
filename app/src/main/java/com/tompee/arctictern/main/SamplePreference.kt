package com.tompee.arctictern.main

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_sample", version = 1)
internal abstract class SamplePreference {

    @ArcticTern.Property
    open var intPreference: Int = 1

    @ArcticTern.Property("key_boolean")
    open var booleanPreference: Boolean = false
}
