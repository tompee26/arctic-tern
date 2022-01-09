package com.tompee.arctictern.booleantest

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
class BooleanPreferenceTest {

    companion object {

        private const val FILENAME: String = "pref_boolean"
        private const val VERSION_KEY: String = "arctic.pref.key.version"
        private const val PROPERTY_KEY: String = "isSuccessful"
    }

    private lateinit var booleanPreference: ArcticTernBooleanPreference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreference = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
        sharedPreference.clearAll()
        booleanPreference =
            ArcticTernManager.getInstance(context).createArcticTernBooleanPreference()
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
        booleanPreference.initialize()
        Assert.assertTrue(sharedPreference.contains(VERSION_KEY))
        Assert.assertTrue(sharedPreference.getInt(VERSION_KEY, 1) == 0)
    }

    @Test
    fun `Test_is_updated`() {
        Assert.assertFalse(booleanPreference.isUpdated)
        booleanPreference.migrate()
        Assert.assertTrue(booleanPreference.isUpdated)
    }

    @Test
    fun `Test_default_value`() {
        Assert.assertFalse(sharedPreference.contains(PROPERTY_KEY))
        Assert.assertFalse(booleanPreference.isSuccessful)
    }

    @Test
    fun `Test_set_get_value`() {
        Assert.assertFalse(booleanPreference.isSuccessful)
        booleanPreference.isSuccessful = true
        Assert.assertTrue(booleanPreference.isSuccessful)
        Assert.assertTrue(booleanPreference.isIsSuccessfulSet)
    }

    @Test
    fun `Test_delete`() {
        Assert.assertFalse(booleanPreference.isSuccessful)
        booleanPreference.isSuccessful = true
        Assert.assertTrue(booleanPreference.isIsSuccessfulSet)
        Assert.assertTrue(booleanPreference.isSuccessful)
        booleanPreference.deleteIsSuccessful()
        Assert.assertFalse(booleanPreference.isIsSuccessfulSet)
        Assert.assertFalse(booleanPreference.isSuccessful)
    }

    @Test
    fun `Test_migration`() {
        Assert.assertFalse(booleanPreference.isSuccessful)
        Assert.assertFalse(booleanPreference.isIsSuccessfulSet)
        booleanPreference.initialize()
        booleanPreference.migrate()
        Assert.assertFalse(booleanPreference.isSuccessful)
        Assert.assertTrue(booleanPreference.isIsSuccessfulSet)
        Assert.assertTrue(sharedPreference.getInt(VERSION_KEY, 1) == 2)
    }
}
