package com.tompee.arctictern.nullableserializertest

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
class NullableSerializerPreferenceTest {

    companion object {

        private const val FILENAME: String = "pref_nullable_serializer"
        private const val VERSION_KEY: String = "arctic.pref.key.version"
        private const val PROPERTY_KEY: String = "key_boxedString"
    }

    private lateinit var serializerPreference: ArcticTernNullableSerializerPreference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreference = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
        sharedPreference.clearAll()
        serializerPreference = ArcticTernManager.getInstance(context).createArcticTernNullableSerializerPreference()
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
        serializerPreference.initialize()
        Assert.assertTrue(sharedPreference.contains(VERSION_KEY))
        Assert.assertTrue(sharedPreference.getInt(VERSION_KEY, 0) == 1)
    }

    @Test
    fun `Test_is_updated`() {
        Assert.assertFalse(serializerPreference.isUpdated)
        serializerPreference.migrate()
        Assert.assertTrue(serializerPreference.isUpdated)
    }

    @Test
    fun `Test_default_value`() {
        Assert.assertFalse(sharedPreference.contains(PROPERTY_KEY))
        Assert.assertTrue(serializerPreference.boxedString == StringWrapper(""))
    }

    @Test
    fun `Test_set_get_value`() {
        Assert.assertTrue(serializerPreference.boxedString == StringWrapper(""))
        serializerPreference.boxedString = StringWrapper("arctictern")
        Assert.assertTrue(serializerPreference.boxedString == StringWrapper("arctictern"))
        Assert.assertTrue(serializerPreference.isBoxedStringSet)
    }

    @Test
    fun `Test_delete`() {
        Assert.assertTrue(serializerPreference.boxedString == StringWrapper(""))
        serializerPreference.boxedString = StringWrapper("arctictern")
        Assert.assertTrue(serializerPreference.isBoxedStringSet)
        Assert.assertTrue(serializerPreference.boxedString == StringWrapper("arctictern"))
        serializerPreference.deleteBoxedString()
        Assert.assertFalse(serializerPreference.isBoxedStringSet)
        Assert.assertTrue(serializerPreference.boxedString == StringWrapper(""))
    }
}
