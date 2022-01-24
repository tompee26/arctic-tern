package com.tompee.arctictern.serializertest

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
class SerializerNullablePreferenceTest {

    companion object {

        private const val FILENAME: String = "pref_serializer_nullable"
        private const val VERSION_KEY: String = "arctic.pref.key.version"
        private const val PROPERTY_KEY: String = "key_boxedInt"
    }

    private lateinit var serializerPreference: ArcticTernSerializerNullablePreference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreference = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
        sharedPreference.clearAll()
        serializerPreference = ArcticTernManager.getInstance(context).createArcticTernSerializerNullablePreference()
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
        ArcticTernSerializerNullablePreference.initialize(context)
        Assert.assertTrue(sharedPreference.contains(VERSION_KEY))
        Assert.assertTrue(sharedPreference.getInt(VERSION_KEY, 1) == 0)
    }

    @Test
    fun `Test_is_updated`() {
        Assert.assertFalse(ArcticTernSerializerNullablePreference.isUpdated(context))
        ArcticTernSerializerNullablePreference.migrate(context)
        Assert.assertTrue(ArcticTernSerializerNullablePreference.isUpdated(context))
    }

    @Test
    fun `Test_default_value`() {
        Assert.assertFalse(sharedPreference.contains(PROPERTY_KEY))
        Assert.assertTrue(serializerPreference.boxedInt == null)
    }

    @Test
    fun `Test_set_get_value`() {
        Assert.assertTrue(serializerPreference.boxedInt == null)
        serializerPreference.boxedInt = IntWrapper(30)
        Assert.assertTrue(serializerPreference.boxedInt == IntWrapper(30))
        Assert.assertTrue(serializerPreference.isBoxedIntSet)
    }

    @Test
    fun `Test_delete`() {
        Assert.assertTrue(serializerPreference.boxedInt == null)
        serializerPreference.boxedInt = IntWrapper(50)
        Assert.assertTrue(serializerPreference.isBoxedIntSet)
        Assert.assertTrue(serializerPreference.boxedInt == IntWrapper(50))
        serializerPreference.deleteBoxedInt()
        Assert.assertFalse(serializerPreference.isBoxedIntSet)
        Assert.assertTrue(serializerPreference.boxedInt == null)
    }
}
