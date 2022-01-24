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
class NullableStringPreferenceTest {

    companion object {

        private const val FILENAME: String = "pref_nullable_string"
        private const val VERSION_KEY: String = "arctic.pref.key.version"
        private const val PROPERTY_KEY: String = "key_name"
    }

    private lateinit var stringPreference: ArcticTernNullableStringPreference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreference = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
        sharedPreference.clearAll()
        stringPreference =
            ArcticTernManager.getInstance(context).createArcticTernNullableStringPreference()
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
        ArcticTernNullableStringPreference.initialize(context)
        Assert.assertTrue(sharedPreference.contains(VERSION_KEY))
        Assert.assertTrue(sharedPreference.getInt(VERSION_KEY, 1) == 0)
    }

    @Test
    fun `Test_is_updated`() {
        Assert.assertFalse(ArcticTernNullableStringPreference.isUpdated(context))
        ArcticTernNullableStringPreference.migrate(context)
        Assert.assertTrue(ArcticTernNullableStringPreference.isUpdated(context))
    }

    @Test
    fun `Test_default_value`() {
        Assert.assertFalse(sharedPreference.contains(PROPERTY_KEY))
        Assert.assertTrue(stringPreference.name == null)
    }

    @Test
    fun `Test_set_get_value`() {
        Assert.assertTrue(stringPreference.name == null)
        stringPreference.name = "ternarctic"
        Assert.assertTrue(stringPreference.name == "ternarctic")
        Assert.assertTrue(stringPreference.isNameSet)
        stringPreference.name = null
        Assert.assertTrue(stringPreference.name == null) // Setting to null is equivalent to remove
        Assert.assertFalse(stringPreference.isNameSet)
    }

    @Test
    fun `Test_delete`() {
        Assert.assertTrue(stringPreference.name == null)
        stringPreference.name = "ternarctic"
        Assert.assertTrue(stringPreference.isNameSet)
        Assert.assertTrue(stringPreference.name == "ternarctic")
        stringPreference.deleteName()
        Assert.assertFalse(stringPreference.isNameSet)
        Assert.assertTrue(stringPreference.name == null)
    }
}
