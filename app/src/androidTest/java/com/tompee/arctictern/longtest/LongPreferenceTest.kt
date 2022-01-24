package com.tompee.arctictern.longtest

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
class LongPreferenceTest {

    companion object {

        private const val FILENAME: String = "pref_long"
        private const val VERSION_KEY: String = "arctic.pref.key.version"
        private const val PROPERTY_KEY: String = "key_timestamp"
    }

    private lateinit var longPreference: ArcticTernLongPreference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreference = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
        sharedPreference.clearAll()
        longPreference = ArcticTernManager.getInstance(context).createArcticTernLongPreference()
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
        ArcticTernLongPreference.initialize(context)
        Assert.assertTrue(sharedPreference.contains(VERSION_KEY))
        Assert.assertTrue(sharedPreference.getInt(VERSION_KEY, 1) == 0)
    }

    @Test
    fun `Test_is_updated`() {
        Assert.assertFalse(ArcticTernLongPreference.isUpdated(context))
        ArcticTernLongPreference.migrate(context)
        Assert.assertTrue(ArcticTernLongPreference.isUpdated(context))
    }

    @Test
    fun `Test_default_value`() {
        Assert.assertFalse(sharedPreference.contains(PROPERTY_KEY))
        Assert.assertTrue(longPreference.timestamp == 123456789L)
    }

    @Test
    fun `Test_set_get_value`() {
        Assert.assertTrue(longPreference.timestamp == 123456789L)
        longPreference.timestamp = 387L
        Assert.assertTrue(longPreference.timestamp == 387L)
        Assert.assertTrue(longPreference.isTimestampSet)
    }

    @Test
    fun `Test_delete`() {
        Assert.assertTrue(longPreference.timestamp == 123456789L)
        longPreference.timestamp = 387L
        Assert.assertTrue(longPreference.isTimestampSet)
        Assert.assertTrue(longPreference.timestamp == 387L)
        longPreference.deleteTimestamp()
        Assert.assertFalse(longPreference.isTimestampSet)
        Assert.assertTrue(longPreference.timestamp == 123456789L)
    }
}
