package com.tompee.arctictern.settest

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
class NullableStringSetPreferenceTest {

    companion object {

        private const val FILENAME: String = "pref_nullable_string_set"
        private const val VERSION_KEY: String = "arctic.pref.key.version"
        private const val PROPERTY_KEY: String = "key_nameSet"
    }

    private lateinit var stringSetPreference: ArcticTernNullableStringSetPreference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreference = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
        sharedPreference.clearAll()
        stringSetPreference =
            ArcticTernManager.getInstance(context).createArcticTernNullableStringSetPreference()
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
        ArcticTernNullableStringSetPreference.initialize(context)
        Assert.assertTrue(sharedPreference.contains(VERSION_KEY))
        Assert.assertTrue(sharedPreference.getInt(VERSION_KEY, 1) == 0)
    }

    @Test
    fun `Test_is_updated`() {
        Assert.assertFalse(ArcticTernNullableStringSetPreference.isUpdated(context))
        ArcticTernNullableStringSetPreference.migrate(context)
        Assert.assertTrue(ArcticTernNullableStringSetPreference.isUpdated(context))
    }

    @Test
    fun `Test_default_value`() {
        Assert.assertFalse(sharedPreference.contains(PROPERTY_KEY))
        Assert.assertTrue(stringSetPreference.nameSet == setOf(null))
    }

    @Test
    fun `Test_set_get_value`() {
        Assert.assertTrue(stringSetPreference.nameSet == setOf(null))
        stringSetPreference.nameSet = setOf("name", null)
        Assert.assertTrue(stringSetPreference.nameSet == setOf("name", null))
        Assert.assertTrue(stringSetPreference.isNameSetSet)
    }

    @Test
    fun `Test_delete`() {
        Assert.assertTrue(stringSetPreference.nameSet == setOf(null))
        stringSetPreference.nameSet = setOf("first", null, "second", null)
        Assert.assertTrue(stringSetPreference.isNameSetSet)
        Assert.assertTrue(stringSetPreference.nameSet == setOf("first", null, "second", null))
        stringSetPreference.deleteNameSet()
        Assert.assertFalse(stringSetPreference.isNameSetSet)
        Assert.assertTrue(stringSetPreference.nameSet == setOf(null))
    }
}
