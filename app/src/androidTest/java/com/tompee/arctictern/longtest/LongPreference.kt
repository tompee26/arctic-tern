package com.tompee.arctictern.longtest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_long", version = 1)
abstract class LongPreference {

    @ArcticTern.Property
    open var timestamp = 123456789L
}
