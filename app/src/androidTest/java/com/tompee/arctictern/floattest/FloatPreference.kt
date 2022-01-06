package com.tompee.arctictern.floattest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_float", version = 1)
abstract class FloatPreference {

    @ArcticTern.Property
    open var temperature = 37.1f
}
