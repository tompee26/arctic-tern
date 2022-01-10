package com.tompee.arctictern.inttest

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.tompee.arctictern.ArcticTernManager
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class IntPreferenceTest {

    companion object {

        private const val FILENAME: String = "pref_int"
        private const val VERSION_KEY: String = "arctic.pref.key.version"
        private const val PROPERTY_KEY: String = "key_counter"
    }

    private lateinit var intPreference: ArcticTernIntPreference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreference = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
        sharedPreference.clearAll()
        intPreference = ArcticTernManager.getInstance(context).createArcticTernIntPreference()
    }

    private fun SharedPreferences.clearAll() {
        edit().clear().commit()
    }

    @After
    fun teardown() {
        sharedPreference.clearAll()
    }

    @Test
    fun `Test_initial_version`() {
        Assert.assertFalse(sharedPreference.contains("arctic.pref.key.version"))
    }

    @Test
    fun `Test_initialization`() {
        intPreference.initialize()
        Assert.assertTrue(sharedPreference.contains(VERSION_KEY))
        Assert.assertTrue(sharedPreference.getInt(VERSION_KEY, 1) == 0)
    }

    @Test
    fun `Test_is_updated`() {
        Assert.assertFalse(intPreference.isUpdated)
        intPreference.migrate()
        Assert.assertTrue(intPreference.isUpdated)
    }

    @Test
    fun `Test_default_value`() {
        Assert.assertFalse(sharedPreference.contains(PROPERTY_KEY))
        Assert.assertTrue(intPreference.counter == 12)
    }

    @Test
    fun `Test_set_get_value`() {
        Assert.assertTrue(intPreference.counter == 12)
        intPreference.counter = 15
        Assert.assertTrue(intPreference.counter == 15)
        Assert.assertTrue(intPreference.isCounterSet)
    }

    @Test
    fun `Test_delete`() {
        Assert.assertTrue(intPreference.counter == 12)
        intPreference.counter = 18
        Assert.assertTrue(intPreference.isCounterSet)
        Assert.assertTrue(intPreference.counter == 18)
        intPreference.deleteCounter()
        Assert.assertFalse(intPreference.isCounterSet)
        Assert.assertTrue(intPreference.counter == 12)
    }
}
