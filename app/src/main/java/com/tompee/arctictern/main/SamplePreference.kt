package com.tompee.arctictern.main

import com.tompee.arctictern.nest.ArcticTern

@ArcticTern(preferenceFile = "pref_sample", version = 1)
internal abstract class SamplePreference {

    @ArcticTern.Property
    open var intPreference: Int = 1

    @ArcticTern.Property("key_boolean")
    open var booleanPreference: Boolean = false

    @ArcticTern.Property
    open var floatPreference: Float = 0.04f

    @ArcticTern.Property("loooong")
    open var longPreference: Long = 123L

    @ArcticTern.Property("myString")
    open var stringPreference: String = "mypreference"

    @ArcticTern.Property
    open var nullableStringPreference: String? = null
}
