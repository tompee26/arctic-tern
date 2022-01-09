package com.tompee.arctictern.stringtest

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
class StringPreferenceTest {

    companion object {

        private const val FILENAME: String = "pref_string"
        private const val VERSION_KEY: String = "arctic.pref.key.version"
        private const val PROPERTY_KEY: String = "key_name"
    }

    private lateinit var stringPreference: ArcticTernStringPreference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreference = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
        sharedPreference.clearAll()
        stringPreference = ArcticTernManager.getInstance(context).createStringPreference()
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
        stringPreference.initialize()
        Assert.assertTrue(sharedPreference.contains(VERSION_KEY))
        Assert.assertTrue(sharedPreference.getInt(VERSION_KEY, 0) == 1)
    }

    @Test
    fun `Test_is_updated`() {
        Assert.assertFalse(stringPreference.isUpdated)
        stringPreference.migrate()
        Assert.assertTrue(stringPreference.isUpdated)
    }

    @Test
    fun `Test_default_value`() {
        Assert.assertFalse(sharedPreference.contains(PROPERTY_KEY))
        Assert.assertTrue(stringPreference.name == "arctictern")
    }

    @Test
    fun `Test_set_get_value`() {
        Assert.assertTrue(stringPreference.name == "arctictern")
        stringPreference.name = "ternarctic"
        Assert.assertTrue(stringPreference.name == "ternarctic")
        Assert.assertTrue(stringPreference.isNameSet)
    }

    @Test
    fun `Test_delete`() {
        Assert.assertTrue(stringPreference.name == "arctictern")
        stringPreference.name = "ternarctic"
        Assert.assertTrue(stringPreference.isNameSet)
        Assert.assertTrue(stringPreference.name == "ternarctic")
        stringPreference.deleteName()
        Assert.assertFalse(stringPreference.isNameSet)
        Assert.assertTrue(stringPreference.name == "arctictern")
    }
}
