package stobix.utils

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * A class to simplify and abstract time handling tasks
 */

class DateHandler() : Parcelable {

    private val calendar: Calendar = Calendar.getInstance() // TODO Localize!

    init {
        calendar.firstDayOfWeek = Calendar.MONDAY
    }

    constructor(timestamp: Long) : this() {
        calendar.timeInMillis = timestamp
    }

    /**
     * Set the time
     */
    fun setTime(hour: Int, minute: Int): DateHandler {
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        return this
    }

    /**
     * Set the date, with a 0-indexed month because reasons
     */
    fun setDate0(year: Int, month0: Int, day: Int): DateHandler {
        calendar.set(year, month0, day)
        return this
    }

    /**
     * A <year,month 1-12,day> Triple
     */
    var date
        get() = Triple(year, month, day)
        set(value) {
            val (year, month, day) = value
            setDate0(year, month-1, day)
        }

    /**
     * A <hour,minute> Pair
     */
    var time
        get() = Pair(hour , minute)
        set(value){
            val (hour, minute) = value
            setTime(hour, minute)
        }

    /**
     * The day the week starts with. Monday is 0
     */
    var weekStartsWith
        get() = (calendar.firstDayOfWeek+5)%7
        set(weekDay) {
            calendar.firstDayOfWeek = (weekDay+2)%7
        }

    /**
     * Day of week, monday=0 to sunday=6
     */
    var weekDay //
        get() = calendar.get(Calendar.DAY_OF_WEEK)
                .minus(2) // The week does not begin with a saturday, but a monday
                .plus(7) // Ensure the values are positive, since rem is broken
                .rem(7) // Assign each week day a sensible, zero-indexed, value
        set(value) {
            calendar.set(
                    Calendar.DAY_OF_WEEK,
                    (value+2)%7
            )
        }

    /**
     * Gets the underlying Date object
     */
    val dateObject: Date
        get() = calendar.time

    /**
     * Gets the hour of the day
     */
    var hour
        get() = calendar.get(Calendar.HOUR_OF_DAY)
        set(value) {
            calendar.set(Calendar.HOUR_OF_DAY, value)
        }

    /**
     *
     */
    var minute
        get() = calendar.get(Calendar.MINUTE)
        set(value) {
            calendar.set(Calendar.MINUTE, value)
        }

    /**
     * Return the year
     */
    var year
        get() = calendar.get(Calendar.YEAR)
        set(value) {
            calendar.set(Calendar.YEAR, value)
        }

    /**
     * Return the month, 0-11!
     */
    var month0
        get() = calendar.get(Calendar.MONTH)
        set(value) {
            calendar.set(Calendar.MONTH, value)
        }

    /**
     * Return the month, 1-12
     */
    var month
        get() = calendar.get(Calendar.MONTH)+1
        set(value) {
            calendar.set(Calendar.MONTH, value-1)
        }

    /**
     * Return the day of the month
     */
    var day
        get() = calendar.get(Calendar.DAY_OF_MONTH)
        set(value) {
            calendar.set(Calendar.DAY_OF_MONTH, value)
        }

    /**
     * "this calendars time value in milliseconds"
     */
    var timestamp
        get() = calendar.timeInMillis
        set(value) {
            calendar.timeInMillis = value
        }

    constructor(parcel: Parcel) : this(parcel.readLong())

    /**
     *  Returns a new copy of the DateHandler
     */
    fun clone() = DateHandler(calendar.timeInMillis)

    // These are used in "dot sequences", eg dateHandler.subtractHours(3).addDays(6)

    @Suppress("unused")
    fun addHours(hours: Int) = addThing(Calendar.HOUR_OF_DAY, hours)

    @Suppress("unused")
    fun addMinutes(minutes: Int) = addThing(Calendar.MINUTE, minutes)

    @Suppress("unused")
    fun addDays(days: Int) = addThing(Calendar.DAY_OF_MONTH, days)

    @Suppress("unused")
    fun addMonths(months: Int) = addThing(Calendar.MONTH, months)

    @Suppress("unused")
    fun addYears(years: Int) = addThing(Calendar.YEAR, years)

    @Suppress("unused")
    fun subtractHours(hours: Int) = addThing(Calendar.HOUR_OF_DAY, -hours)

    @Suppress("unused")
    fun subtractMinutes(minutes: Int) = addThing(Calendar.MINUTE, -minutes)

    @Suppress("unused")
    fun subtractDays(days: Int) = addThing(Calendar.DAY_OF_MONTH, -days)

    @Suppress("unused")
    fun subtractMonths(months: Int) = addThing(Calendar.MONTH, -months)

    @Suppress("unused")
    fun subtractYears(years: Int) = addThing(Calendar.YEAR, -years)

    private fun addThing(thingField: Int, thing: Int): DateHandler {
        calendar.add(thingField, thing)
        return this
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DateHandler> {
        override fun createFromParcel(parcel: Parcel): DateHandler {
            return DateHandler(parcel.readLong())
        }

        override fun newArray(size: Int): Array<DateHandler?> {
            return arrayOfNulls(size)
        }
    }
}
