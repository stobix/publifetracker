package stobix.app.lifetracker

import java.util.Calendar

/**
 * Created by stobix on 11/28/17.
 * A class to simplify and abstract the time handling that is needed in this project.
 */

class DateHandler internal constructor() {

    private var internal_cal: Calendar = Calendar.getInstance() // TODO Localize!

    constructor(timestamp: Long) : this() {
        internal_cal.timeInMillis=timestamp
    }

    internal fun setTime(hour: Int, minute: Int) {
        internal_cal.set(Calendar.HOUR_OF_DAY, hour)
        internal_cal.set(Calendar.MINUTE, minute)
    }

    internal fun setDate(year: Int, month: Int, day: Int) = internal_cal.set(year,month,day)
   /* {
        internal_cal.set(Calendar.YEAR,year)
        internal_cal.set(Calendar.MONTH,month)
        internal_cal.set(Calendar.DAY_OF_MONTH,day)
    } */

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
