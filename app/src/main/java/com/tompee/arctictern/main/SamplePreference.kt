package com.tompee.arctictern.main

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(filename = "pref_sample", version = 1)
internal interface SamplePreference {

    @ArcticTern.Property
    val x: Int
}
