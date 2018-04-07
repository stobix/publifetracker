package stobix.utils

import java.util.Calendar

/**
 * A class to simplify and abstract time handling tasks
 */

class DateHandler() {

    private var internal_cal: Calendar = Calendar.getInstance() // TODO Localize!

    constructor(timestamp: Long) : this() {
        internal_cal.timeInMillis=timestamp
    }

    fun setTime(hour: Int, minute: Int) : DateHandler {
        internal_cal.set(Calendar.HOUR_OF_DAY, hour)
        internal_cal.set(Calendar.MINUTE, minute)
        return this
    }

    fun setDate(year: Int, month: Int, day: Int) : DateHandler  {
        internal_cal.set(year, month, day)
        return this
    }

    var date
        get() = Triple(year,month,hour)
        set(value) {
            val (year, month, hour) = value
            setDate(year, month, hour)
        }

    val dateObject
        get() = internal_cal.time

    var hour
        get() = internal_cal.get(Calendar.HOUR_OF_DAY)
        set(value) {internal_cal.set(Calendar.HOUR_OF_DAY,value)}

    var minute
        get() = internal_cal.get(Calendar.MINUTE)
        set(value) {internal_cal.set(Calendar.MINUTE,value)}

    var year
        get() = internal_cal.get(Calendar.YEAR)
        set(value) {internal_cal.set(Calendar.YEAR,value)}

    var month
        get() = internal_cal.get(Calendar.MONTH)
        set(value) {internal_cal.set(Calendar.MONTH,value)}

    var day
        get() = internal_cal.get(Calendar.DAY_OF_MONTH)
        set(value) {internal_cal.set(Calendar.DAY_OF_MONTH,value)}

    var timestamp
        get()=internal_cal.timeInMillis
        set(value) {internal_cal.timeInMillis=value}

    fun clone() = DateHandler(internal_cal.timeInMillis)

    fun addHours(hours: Int) = addThing(Calendar.HOUR_OF_DAY,hours)
    fun addMinutes(minutes: Int) = addThing(Calendar.MINUTE,minutes)
    fun addDays(days: Int) = addThing(Calendar.DAY_OF_MONTH,days)
    fun addMonths(months: Int) = addThing(Calendar.MONTH,months)
    fun addYears(years: Int) = addThing(Calendar.MONTH,years)

    fun subtractHours(hours: Int) = addThing(Calendar.HOUR_OF_DAY,-hours)
    fun subtractMinutes(minutes: Int) = addThing(Calendar.MINUTE,-minutes)
    fun subtractDays(days: Int) = addThing(Calendar.DAY_OF_MONTH,-days)
    fun subtractMonths(months: Int) = addThing(Calendar.MONTH,-months)
    fun subtractYears(years: Int) = addThing(Calendar.MONTH,-years)

    private fun addThing(thingField:Int,thing: Int) : DateHandler {
        internal_cal.add(thingField,thing)
        return this
    }
}
