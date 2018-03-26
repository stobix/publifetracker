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

    fun setTime(hour: Int, minute: Int) {
        internal_cal.set(Calendar.HOUR_OF_DAY, hour)
        internal_cal.set(Calendar.MINUTE, minute)
    }

    fun setDate(year: Int, month: Int, day: Int) = internal_cal.set(year,month,day)

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
}
