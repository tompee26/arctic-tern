package com.tompee.arctictern.floattest

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
class FloatPreferenceTest {

    companion object {

        private const val FILENAME: String = "pref_float"
        private const val VERSION_KEY: String = "arctic.pref.key.version"
        private const val PROPERTY_KEY: String = "key_temperature"
    }

    private lateinit var floatPreference: ArcticTernFloatPreference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreference = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
        sharedPreference.clearAll()
        floatPreference = ArcticTernManager.getInstance(context).createArcticTernFloatPreference()
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
        floatPreference.initialize()
        Assert.assertTrue(sharedPreference.contains(VERSION_KEY))
        Assert.assertTrue(sharedPreference.getInt(VERSION_KEY, 1) == 0)
    }

    @Test
    fun `Test_is_updated`() {
        Assert.assertFalse(floatPreference.isUpdated)
        floatPreference.migrate()
        Assert.assertTrue(floatPreference.isUpdated)
    }

    @Test
    fun `Test_default_value`() {
        Assert.assertFalse(sharedPreference.contains(PROPERTY_KEY))
        Assert.assertTrue(floatPreference.temperature == 37.1f)
    }

    @Test
    fun `Test_set_get_value`() {
        Assert.assertTrue(floatPreference.temperature == 37.1f)
        floatPreference.temperature = 15f
        Assert.assertTrue(floatPreference.temperature == 15f)
        Assert.assertTrue(floatPreference.isTemperatureSet)
    }

    @Test
    fun `Test_delete`() {
        Assert.assertTrue(floatPreference.temperature == 37.1f)
        floatPreference.temperature = 38f
        Assert.assertTrue(floatPreference.isTemperatureSet)
        Assert.assertTrue(floatPreference.temperature == 38f)
        floatPreference.deleteTemperature()
        Assert.assertFalse(floatPreference.isTemperatureSet)
        Assert.assertTrue(floatPreference.temperature == 37.1f)
    }

    @Test
    fun `Test_migration`() {
        Assert.assertTrue(floatPreference.temperature == 37.1f)
        Assert.assertFalse(floatPreference.isTemperatureSet)
        floatPreference.initialize()
        floatPreference.migrate()
        Assert.assertTrue(floatPreference.temperature == 37.1f)
        Assert.assertFalse(floatPreference.isTemperatureSet)
        Assert.assertTrue(sharedPreference.getInt(VERSION_KEY, 1) == 1)
    }
}
