package com.tompee.arctictern.floattest

import android.content.SharedPreferences
import com.tompee.arctictern.nest.ArcticTern
import com.tompee.arctictern.nest.Migration

@ArcticTern(preferenceFile = "pref_float", version = 1)
abstract class FloatPreference {

    @ArcticTern.Migration(version = 2)
    class IncreaseMigration : Migration {
        override fun onMigrate(version: Int, sharedPreferences: SharedPreferences) {
            sharedPreferences.edit().remove("key_temperature").commit()
        }
    }

    @ArcticTern.Property
    open var temperature = 37.1f
}
