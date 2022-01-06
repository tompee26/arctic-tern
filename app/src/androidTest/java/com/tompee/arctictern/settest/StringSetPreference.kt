package com.tompee.arctictern.settest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_string_set", version = 1)
abstract class StringSetPreference {

    @ArcticTern.Property
    open var nameSet = setOf("name")
}
