package com.tompee.arctictern.nest

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

/**
 * Arctic Tern Preference contract. This is backed by [SharedPreferences].
 * Provides the usual getter, setter, delete methods and a way to observe changes
 * in value using [Flow]
 *
 * @property key the unique preference key
 * @property defaultValue the default value of this preference
 * @property valueProvider a function that when invoked will provide the current value
 *                         of this preference
 * @property valueSetter a function that when invoked will update the current value of
 *                       this preference
 * @property sharedPreferences the [SharedPreferences] instance
 */
class Preference<T>(
    val key: String,
    val defaultValue: T,
    private val valueProvider: (SharedPreferences, String, T) -> T,
    private val valueSetter: (SharedPreferences, String, T) -> Unit,
    private val sharedPreferences: SharedPreferences,
) {

    /**
     * Updates/Returns the current value
     */
    var value: T
        get() = valueProvider.invoke(sharedPreferences, key, defaultValue)
        set(value) = valueSetter.invoke(sharedPreferences, key, value)

    /**
     * Returns true if this preference has a current value set
     */
    val isSet: Boolean
        get() = sharedPreferences.contains(key)

    /**
     * Deletes the value and the entry denoted by [key]
     */
    fun delete() {
        sharedPreferences.edit { remove(key) }
    }

    /**
     * Builds a flow that emits items on every value change
     */
    private fun updateFlow(): Flow<T> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            trySendBlocking(key)
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
        .filter { it == key || it == null }
        .map { value }

    /**
     * Returns a flow that when subscribed to will emit the update value of this preference
     */
    fun observe(): Flow<T> = updateFlow()
        .onStart { emit(value) }
        .conflate()

    /**
     * Returns a state flow with the [value] as the default value
     */
    fun asStateFlow(scope: CoroutineScope, started: SharingStarted): StateFlow<T> {
        return updateFlow().stateIn(scope, started, value)
    }

    /**
     * Returns a shared flow with the [defaultValue] as the default value
     */
    fun asSharedFlow(scope: CoroutineScope, started: SharingStarted, replay: Int): SharedFlow<T> {
        return observe().shareIn(scope, started, replay)
    }

    /**
     * Returns a flow collector
     */
    fun asFlowCollector(): FlowCollector<T> {
        return FlowCollector { value = it }
    }
}
