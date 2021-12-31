package com.tompee.arctictern.nest.preference

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

/**
 * Preference Wrapper implementation
 */
internal class Preference<T>(
    val key: String,
    val defaultValue: T,
    private val valueProvider: (SharedPreferences) -> T,
    private val valueSetter: (SharedPreferences, T) -> Unit,
    private val sharedPreferences: SharedPreferences
) {

    /**
     * Returns the current value of this preference
     */
    val value: T
        get() = valueProvider.invoke(sharedPreferences)

    /**
     * Returns true if this preference has a current value set
     */
    val isSet: Boolean
        get() = sharedPreferences.contains(key)

    /**
     * Sets the [value] to this preference in an eager fashion
     */
    fun set(value: T) {
        valueSetter.invoke(sharedPreferences, value)
    }

    /**
     * Deletes this preference in the file
     */
    fun delete() {
        sharedPreferences.edit { remove(key) }
    }

    /**
     * Returns a flow that you can use to observe the values of this preference
     */
    fun observe(): Flow<T> {
        return callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                trySend(key)
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
        }
            .filter { it == key || it == null }
            .onStart { emit("") } // Just to trigger the first emission
            .map { value }
            .conflate()
    }
}