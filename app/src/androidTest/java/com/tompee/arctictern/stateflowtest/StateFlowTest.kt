package com.tompee.arctictern.stateflowtest

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.tompee.arctictern.ArcticTernManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@MediumTest
class StateFlowTest {

    companion object {

        private const val FILENAME: String = "pref_boolean"
    }

    private lateinit var stateFlowPreference: ArcticTernStateFlowPreference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreference = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
        sharedPreference.clearAll()
        stateFlowPreference =
            ArcticTernManager.getInstance(context).createArcticTernStateFlowPreference()
    }

    private fun SharedPreferences.clearAll() {
        edit().clear().commit()
    }

    @After
    fun teardown() {
        sharedPreference.clearAll()
    }

    @Test
    fun `Test_StateFlow`() = runTest {
        Assert.assertFalse(stateFlowPreference.isSuccessful)
        val stateFlow = stateFlowPreference
            .isSuccessfulAsStateFlow(this, SharingStarted.Eagerly)
        advanceUntilIdle()

        val set = stateFlow.first()
        Assert.assertFalse(set)

        stateFlowPreference.isSuccessful = true
        stateFlow.first { it }

        stateFlowPreference.isSuccessful = false
        stateFlow.first { !it }

        coroutineContext.cancelChildren()
    }

    @Test
    fun `Test_StateFlow_Default_Value`() = runTest {
        Assert.assertFalse(stateFlowPreference.isSuccessful)
        val stateFlow = stateFlowPreference
            .isSuccessfulAsStateFlow(this, SharingStarted.Lazily)

        val set = stateFlow.first()
        Assert.assertFalse(set)
        advanceUntilIdle()

        stateFlowPreference.isSuccessful = true
        stateFlow.first { it }

        val newStateFlow = stateFlowPreference
            .isSuccessfulAsStateFlow(this, SharingStarted.WhileSubscribed())
        val isSet = newStateFlow.first()
        Assert.assertTrue(isSet)

        coroutineContext.cancelChildren()
    }
}
