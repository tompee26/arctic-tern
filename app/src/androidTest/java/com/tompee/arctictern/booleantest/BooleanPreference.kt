package com.tompee.arctictern.booleantest

import android.content.SharedPreferences
import com.tompee.arctictern.nest.ArcticTern
import com.tompee.arctictern.nest.Migration

@ArcticTern(preferenceFile = "pref_boolean", version = 2)
abstract class BooleanPreference {

    @ArcticTern.Migration(version = 2)
    class ResetMigration : Migration {
        override fun onMigrate(version: Int, sharedPreferences: SharedPreferences) {
            sharedPreferences.edit().putBoolean("isSuccessful", false).commit()
        }
    }

    @ArcticTern.Migration(version = 1)
    object SetMigration : Migration {
        override fun onMigrate(version: Int, sharedPreferences: SharedPreferences) {
            sharedPreferences.edit().putBoolean("isSuccessful", true).commit()
        }
    }

    @ArcticTern.Property(key = "isSuccessful")
    open var isSuccessful = false
}
