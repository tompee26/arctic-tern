package com.tompee.arctictern.main

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_sample", version = 1)
internal abstract class SamplePreference {

    @ArcticTern.Property("cloud_x")
    open var x: Int = 1
}
