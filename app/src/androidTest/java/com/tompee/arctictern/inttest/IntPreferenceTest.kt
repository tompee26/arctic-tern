package com.tompee.arctictern.inttest

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.tompee.arctictern.ArcticTernManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@MediumTest
class IntPreferenceTest {

    companion object {

        private const val VERSION_KEY: String = "arctic.pref.key.version"
        private const val PROPERTY_KEY: String = "key_counter"
    }

    private lateinit var intPreference: ArcticTernIntPreference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clearAllSharedPreferences(context)
        sharedPreference = context.getSharedPreferences("pref_int", Context.MODE_PRIVATE)
        intPreference = ArcticTernManager.getInstance(context).createArcticTernIntPreference()
    }

    private fun clearAllSharedPreferences(context: Context) {
        val sharedPreferencesPath =
            File(context.filesDir.parentFile!!.absolutePath + File.separator + "shared_prefs")
        sharedPreferencesPath.listFiles()?.forEach { file ->
            context.getSharedPreferences(file.nameWithoutExtension, Context.MODE_PRIVATE)
                .edit { clear() }
        }
    }

    @Test
    fun `Test_initial_version`() {
        Assert.assertFalse(sharedPreference.contains("arctic.pref.key.version"))
    }

    @Test
    fun `Test_initialization`() {
        intPreference.initialize()
        Assert.assertTrue(sharedPreference.contains(VERSION_KEY))
        Assert.assertTrue(sharedPreference.getInt(VERSION_KEY, 0) == 1)
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
