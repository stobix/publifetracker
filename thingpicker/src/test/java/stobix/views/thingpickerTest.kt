package stobix.views

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class thingpickerTest{

    @Test
    fun appShouldntCrashWhenLoadingThingpicker(){
        val a = Robolectric.setupActivity(MainActivity::class.java)
        val t = a.findViewById<ThingPicker>(R.id.thingPicker)
        assertTrue(t.isLaidOut)

    }

}