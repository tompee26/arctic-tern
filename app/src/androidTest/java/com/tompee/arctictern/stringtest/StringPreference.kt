package com.tompee.arctictern.stringtest

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_string", version = 1)
abstract class StringPreference {

    @ArcticTern.Property
    open var name = "arctictern"
}
