package stobix.utils

import java.util.Calendar

/**
 * A class to simplify and abstract time handling tasks
 */

class DateHandler() {

    private val calendar: Calendar = Calendar.getInstance() // TODO Localize!

    constructor(timestamp: Long) : this() {
        calendar.timeInMillis=timestamp
    }

    fun setTime(hour: Int, minute: Int) : DateHandler {
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        return this
    }

    fun setDate(year: Int, month: Int, day: Int) : DateHandler  {
        calendar.set(year, month, day)
        return this
    }

    var date
        get() = Triple(year,month,hour)
        set(value) {
            val (year, month, hour) = value
            setDate(year, month, hour)
        }

    val weekDay // Day of week, monday=0 to sunday=6
        get() = (calendar.get(Calendar.DAY_OF_WEEK)-2)
                .rem(7) // This might be negative
                .plus(7).rem(7) // And this flips negative values to the correct positive values, like a real mod operation would.

    val dateObject
        get() = calendar.time

    var hour
        get() = calendar.get(Calendar.HOUR_OF_DAY)
        set(value) {calendar.set(Calendar.HOUR_OF_DAY,value)}

    var minute
        get() = calendar.get(Calendar.MINUTE)
        set(value) {calendar.set(Calendar.MINUTE,value)}

    var year
        get() = calendar.get(Calendar.YEAR)
        set(value) {calendar.set(Calendar.YEAR,value)}

    var month
        get() = calendar.get(Calendar.MONTH)
        set(value) {calendar.set(Calendar.MONTH,value)}

    var day
        get() = calendar.get(Calendar.DAY_OF_MONTH)
        set(value) {calendar.set(Calendar.DAY_OF_MONTH,value)}

    var timestamp
        get()=calendar.timeInMillis
        set(value) {calendar.timeInMillis=value}

    fun clone() = DateHandler(calendar.timeInMillis)

    @Suppress("unused")
    fun addHours(hours: Int) = addThing(Calendar.HOUR_OF_DAY,hours)
    @Suppress("unused")
    fun addMinutes(minutes: Int) = addThing(Calendar.MINUTE,minutes)
    @Suppress("unused")
    fun addDays(days: Int) = addThing(Calendar.DAY_OF_MONTH,days)
    @Suppress("unused")
    fun addMonths(months: Int) = addThing(Calendar.MONTH,months)
    @Suppress("unused")
    fun addYears(years: Int) = addThing(Calendar.MONTH,years)

    @Suppress("unused")
    fun subtractHours(hours: Int) = addThing(Calendar.HOUR_OF_DAY,-hours)
    @Suppress("unused")
    fun subtractMinutes(minutes: Int) = addThing(Calendar.MINUTE,-minutes)
    @Suppress("unused")
    fun subtractDays(days: Int) = addThing(Calendar.DAY_OF_MONTH,-days)
    @Suppress("unused")
    fun subtractMonths(months: Int) = addThing(Calendar.MONTH,-months)
    @Suppress("unused")
    fun subtractYears(years: Int) = addThing(Calendar.MONTH,-years)

    private fun addThing(thingField:Int,thing: Int) : DateHandler {
        calendar.add(thingField,thing)
        return this
    }
}
