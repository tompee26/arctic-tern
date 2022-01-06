package com.tompee.arctictern.inttest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_int", version = 1)
abstract class IntPreference {

    @ArcticTern.Property
    open var counter = 12
}
