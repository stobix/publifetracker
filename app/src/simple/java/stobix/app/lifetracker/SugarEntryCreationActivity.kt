package stobix.app.lifetracker

import android.annotation.SuppressLint
import android.app.DialogFragment
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.widget.AppCompatImageView
import android.util.Log.d
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import stobix.utils.DateHandler
import stobix.utils.kotlinExtensions.to
import java.lang.NumberFormatException


/**
 * The list of views that are hidden when the views in HiddenList are shown
 */
typealias ShownList = List<View>

/**
 * The list of views that are hidden when the views in ShownList are shown
 */
typealias HiddenList = List<View>

/**
 * An ID to a Resource
 */
typealias ResourceID = Int

/**
 * A dialog for creating an entry to the blood sugar database
 * Created by stobix on 11/19/17.
 */
@Suppress("NAME_SHADOWING")
open class SugarEntryCreationActivity
@SuppressLint("ValidFragment") constructor
(
) : DialogFragment() {

    private var date: DateHandler = DateHandler()
    private var endDate: DateHandler? = null
    private var alreadyDefinedEntry: Boolean = false
    private var originalTimestamp: Timestamp = 0
    private lateinit var startDateView: TextView
    private lateinit var startTimeView: TextView
    private lateinit var endDateView: TextView
    private lateinit var endTimeView: TextView
    private lateinit var sugarView: TextView
    private lateinit var weightView: TextView
    private lateinit var entry: SugarEntry
    private lateinit var foodView: TextView
    private lateinit var insulinView: TextView
    private lateinit var drinkView: TextView
    private lateinit var extraView: TextView
    private lateinit var categoryView: TextView
    private lateinit var pillsView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alreadyDefinedEntry = arguments.getBoolean("EditCurrent")
        if (alreadyDefinedEntry) {
            entry = arguments.getParcelable("entry")
            date.timestamp = entry.timestamp
            entry.endTimestamp?.let { ts ->
                endDate = DateHandler()
                endDate?.timestamp = ts
            }
            originalTimestamp = entry.timestamp
            d(
                    "SugarEntry create",
                    "already defined; timestamp:${entry.timestamp}, sugar: ${entry.sugarLevel}, extra: ${entry.timestamp}"
            )
        } else {
            date.timestamp = arguments.getLong("timestamp")
            d("SugarEntry create", "creating new, timestamp:${date.timestamp}")
            entry = SugarEntry(timestamp = date.timestamp)
        }
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater?.inflate(R.layout.activity_sugar_entry_creation, container, false)

        v ?: throw Error("could not create view")

        startDateView = v.findViewById(R.id.entryCreatorDate)
        startTimeView = v.findViewById(R.id.entryCreatorTime)
        endDateView = v.findViewById(R.id.entryCreatorEndDate)
        endTimeView = v.findViewById(R.id.entryCreatorEndTime)
        sugarView = v.findViewById(R.id.entryCreatorSugar)
        weightView = v.findViewById(R.id.entryCreatorWeight)
        foodView = v.findViewById(R.id.entryCreatorFood)
        insulinView = v.findViewById(R.id.entryCreatorInsulin)
        pillsView = v.findViewById(R.id.entryCreatorPills)
        drinkView = v.findViewById(R.id.entryCreatorDrink)
        categoryView = v.findViewById(R.id.entryCreatorCategory)
        extraView = v.findViewById(R.id.entryCreatorExtra)

        val stateArray: MutableMap<Int, Boolean> = mutableMapOf()


        // Simulates a quick click on a component, e.g. for giving focus to a text field.
        fun View.click() {
            this.dispatchTouchEvent(
                    MotionEvent.obtain(
                            SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_DOWN, 0f, 0f, 0
                    )
            )
            this.dispatchTouchEvent(
                    MotionEvent.obtain(
                            SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_UP, 0f, 0f, 0
                    )
            )
        }


        fun vis(b: Boolean) =
                when (b) {
                    true  -> View.VISIBLE
                    false -> View.GONE
                }


        fun View.toggleBackgroundImage(b: Boolean) {
            this.setBackgroundResource(
                    if (b) R.drawable.icon_back_circle else R.drawable.icon_back_circle_inactive
            )
        }

        fun ResourceID.toggle() {
            val view = v.findViewById<ImageView>(this)
            view.click()
        }

        // Make the resource run the listener function on each click on the resource, with a boolean toggled each time.
        infix fun <A> Pair<ResourceID, A?>.togglingWithFun(listener: (Boolean)->Unit) {
            val view = v.findViewById<ImageView>(first)
            val truthiness = second != null

            val toggleFun = {
                val visibleValue = stateArray[first] ?: truthiness
                view.toggleBackgroundImage(visibleValue)
                listener(visibleValue)
                stateArray[first] = !visibleValue
            }

            toggleFun()

            view.setOnClickListener { toggleFun() }
        }

        // Toggles between showing the views in shown and hidden, giving focus to activated when shown list is shown
        // Initial state shown is given by whether the property A is defined
        infix fun <A> Pair<ResourceID, A?>.togglingWithoutFocus(shownHidden: Pair<ShownList, HiddenList>) {
            val (shown, hidden) = shownHidden

            this togglingWithFun {
                val hiddenState = vis(it)
                val notHiddenState = vis(!it)
                shown.forEach { it.visibility = hiddenState }
                hidden.forEach { it.visibility = notHiddenState }
            }

        }

        // Stripped down versions

        infix fun <A> Pair<ResourceID, A?>.togglingWithoutFocus(shown: ShownList) = this togglingWithoutFocus (shown to emptyList())

        infix fun ResourceID.togglingWithoutFocus(shownHidden: Pair<ShownList, HiddenList>) = this to true togglingWithoutFocus shownHidden

        infix fun ResourceID.togglingWithoutFocus(shown: ShownList) = this to true togglingWithoutFocus shown


        // Toggles on a click on the Resource between showing the views in shown and hidden, giving focus to activated when shown list is shown (or first in shown list if activated is null)
        // Initial state shown is given by whether the property A is defined
        infix fun <A> Pair<ResourceID, A?>.toggling(shownHiddenActivated: Triple<ShownList, HiddenList, View?>) {
            var (shown, hidden, activated) = shownHiddenActivated
            activated = activated ?: shown.first()

            this togglingWithFun {
                val hiddenState = vis(it)
                val notHiddenState = vis(!it)
                shown.forEach { it.visibility = hiddenState }
                hidden.forEach { it.visibility = notHiddenState }
                if (it) {
                    activated.requestFocus()
                    activated.click()
                }
            }
        }

        // Default to set focus to first in shown list, include a hidden list
        infix fun <A> Pair<ResourceID, A?>.toggling(p: Pair<ShownList, HiddenList>): Unit =
                this toggling (p to null)

        // Default to set focus to first in shown list, have nothing in hidden list
        infix fun <A> Pair<ResourceID, A?>.toggling(shown: ShownList): Unit =
                this toggling (shown to emptyList())

        /**
         * Just declarative syntax sugar, kinda like => in perl, but converts its second argument from an id to a View
         */
        infix fun View.withLabel(id: ResourceID) = listOf(this, v.findViewById(id))


        val buttonClearEndTime: AppCompatImageView = v.findViewById(R.id.entryCreatorEndDateDelete)


        R.id.entryCreatorToggleDateTime to entry.endTimestamp togglingWithoutFocus (
                // (startDateView withLabel R.id.entryCreatorDateLabel) + (startTimeView withLabel R.id.entryCreatorTimeLabel)
                (endDateView withLabel R.id.entryCreatorEndTimeLabel)
                        +(endTimeView withLabel R.id.entryCreatorStartTimeLabel)
                        +buttonClearEndTime
                )

        R.id.entryCreatorWeightToggle to entry.weight toggling (weightView withLabel R.id.entryCreatorWeightLabel)

        R.id.entryCreatorDrinkToggle to entry.drink toggling (drinkView withLabel R.id.entryCreatorDrinkLabel)

        R.id.entryCreatorFoodToggle to entry.food toggling (foodView withLabel R.id.entryCreatorFoodLabel)

        R.id.entryCreatorInsulinToggle to entry.insulin toggling (insulinView withLabel R.id.entryCreatorInsulinLabel)

        R.id.entryCreatorPillsToggle to entry.treatment toggling (pillsView withLabel R.id.entryCreatorPillsLabel)

        R.id.entryCreatorSugarToggle to entry.sugarLevel toggling (sugarView withLabel R.id.entryCreatorSugarLabel)

        // R.id.entryCreatorExtraToggle to entry.extra toggling (extraView withLabel R.id.entryCreatorExtraLabel)

        R.id.entryCreatorExtraToggle to entry.extra toggling (extraView withLabel R.id.entryCreatorExtraLabel)

        v.findViewById<View>(R.id.entryCreatorToggleDateTime).setOnLongClickListener {
            setEndTimes(DateHandler())
            onSubmitAndClose()
            true
        }

        val dateText = "%d-%02d-%02d".format(date.year, date.month, date.day)
        val timeText = "%02d:%02d".format(date.hour, date.minute)
        startDateView.text = dateText
        startTimeView.text = timeText
        setEndTimes(endDate)

        val buttonAdd: Button = v.findViewById(R.id.entrySubmitAction2)
        val buttonAddClose: Button = v.findViewById(R.id.entrySubmitAction1)

        if (alreadyDefinedEntry) {
            sugarView.text = entry.sugarLevel?.toFloat()?.div(10f)?.toString()
            weightView.text = entry.weight?.toFloat()?.div(10f)?.toString()
            extraView.text = entry.extra
            categoryView.text = entry.category
            foodView.text = entry.food
            insulinView.text = entry.insulin?.toString()
            pillsView.text = entry.treatment
            drinkView.text = entry.drink
            buttonAddClose.text = getString(R.string.edit_dialog_button_update)
            buttonAdd.text = getString(R.string.edit_dialog_button_delete)
        } else {
            buttonAddClose.text = getString(R.string.creation_dialog_button_add_close)
            buttonAdd.visibility = View.VISIBLE
        }

        startDateView.setOnClickListener {
            (activity as MainActivity).showDatePicker(
                    PickedType.start.ordinal, date
            )
        }
        startTimeView.setOnClickListener {
            (activity as MainActivity).showTimePicker(
                    PickedType.start.ordinal, date
            )
        }

        endDateView.setOnClickListener {
            (activity as MainActivity).showDatePicker(
                    PickedType.end.ordinal,
                    endDate ?: DateHandler()
            )
        }

        endTimeView.setOnClickListener {
            (activity as MainActivity).showTimePicker(
                    PickedType.end.ordinal,
                    endDate ?: DateHandler()
            )
        }

        buttonAdd.setOnClickListener { onSubmit() }
        buttonAddClose.setOnClickListener { onSubmitAndClose() }
        buttonClearEndTime.setOnClickListener {
            setEndTimes(null)
            endDate = null
        }

        val actionSleep: ImageView = v.findViewById(R.id.entryCreatorSleepAction)

        actionSleep.setOnClickListener {
            val slepStr = getString(R.string.EntryCreatorSleepString)
            categoryView.text = slepStr
            onSubmitAndClose()
        }

        actionSleep.visibility = when {
            entry.category == null -> View.VISIBLE
            entry.category != getString(R.string.EntryCreatorSleepString) -> View.VISIBLE
            else -> View.GONE
        }

        fun ResourceID.sleepStarInit(n: Int) {
            v.findViewById<ImageView>(this).also {
                val slepStr = getString(R.string.EntryCreatorSleepString)
                it.setOnClickListener {
                    // If text is a number, replace it
                    var mebbehParsed = extraView.text?.toString()?.let{
                        try{
                            it.toInt()
                        } catch (e: NumberFormatException){
                            null
                        }
                    }
                    when {
                        mebbehParsed != null -> extraView.text = "$n"
                        extraView.text.isNullOrBlank() -> extraView.text = "$n"
                        else  -> extraView.text = "$n, ${extraView.text}"
                    }
                    setEndTimes(DateHandler())
                    onSubmitAndClose()
                }
                // TODO set visible also if it already has a grade.
                it.visibility = if (entry.category == slepStr) View.VISIBLE else View.GONE
            }
        }

        R.id.entryCreatorSleepAction0.sleepStarInit(0)
        R.id.entryCreatorSleepAction1.sleepStarInit(1)
        R.id.entryCreatorSleepAction2.sleepStarInit(2)
        R.id.entryCreatorSleepAction3.sleepStarInit(3)
        R.id.entryCreatorSleepAction4.sleepStarInit(4)
        R.id.entryCreatorSleepAction5.sleepStarInit(5)

        fun ResourceID.actionButtonInit(s: String) =
                v.findViewById<ImageView>(this).run {
                    setOnClickListener {
                        categoryView.text = s
                        onSubmitAndClose()
                    }
                    visibility = if (entry.category?.let { it != s } ?: true)  View.VISIBLE else View.GONE
                }

        R.id.entryCreatorWalkToJobAction.actionButtonInit("\uD83D\uDEB6Jobb")
        R.id.entryCreatorBikeToJobAction.actionButtonInit("\uD83D\uDEB4Jobb")

        R.id.entryCreatorJobAction.actionButtonInit("Jobb")

        R.id.entryCreatorBikeFromJobAction.actionButtonInit("\uD83D\uDEB4Hem")
        R.id.entryCreatorWalkFromJobAction.actionButtonInit("\uD83D\uDEB6Hem")

        return v

    }

    @Suppress("KDocMissingDocumentation", "EnumEntryName")
    enum class PickedType { start, end }


    // Sending a full SugarEntry since I'm not sure what fields it will contain in the future.
    interface OnSugarEntryEnteredHandler {
        fun onSugarEntryEntered(s: SugarEntry)
    }

    interface OnSugarEntryChangedHandler {
        fun onSugarEntryChanged(s: SugarEntry, originalTimestamp: Long)
    }

    interface OnSugarEntryDeletedHandler {
        fun onSugarEntryDeleted(s: SugarEntry)
    }

    private fun onSubmit() {
        if (alreadyDefinedEntry) {
            // delete the thing
            (activity as OnSugarEntryDeletedHandler).onSugarEntryDeleted(entry)
            dismiss()
        } else {
            handleSubmission()
            // Ensure we don't enter two entries with the same timestamp
            entry = SugarEntry(timestamp = entry.timestamp+1)
        }
    }

    private fun onSubmitAndClose() {
        handleSubmission()
        this.dismiss()
    }

    private fun String?.nullIfEmpty() = if (this.isNullOrEmpty()) null else this

    // Handles the submission of the entered data, either creating a new SugarEntry or updating the existing one.
    private fun handleSubmission() {
        entry.timestamp = date.timestamp
        entry.endTimestamp = endDate?.timestamp
        entry.sugarLevel = sugarView.text?.toString()?.toFloatOrNull()?.times(10)?.toInt()
        entry.weight = weightView.text?.toString()?.toFloatOrNull()?.times(10)?.toInt()
        entry.extra = extraView.text?.toString().nullIfEmpty()
        entry.category = categoryView.text?.toString().nullIfEmpty()
        entry.insulin = insulinView.text?.toString()?.toDoubleOrNull()
        entry.treatment = pillsView.text?.toString().nullIfEmpty()
        entry.food = foodView.text?.toString().nullIfEmpty()
        entry.drink = drinkView.text?.toString().nullIfEmpty()
        if (alreadyDefinedEntry) {
            d(
                    "SugarEntry update",
                    "t ${entry.timestamp} s ${entry.sugarLevel} w ${entry.weight} e ${entry.extra} f ${entry.food} d ${entry.drink} tr ${entry.treatment} i ${entry.insulin}"
            )
            (activity as OnSugarEntryChangedHandler).onSugarEntryChanged(entry, originalTimestamp)
        } else {
            d(
                    "SugarEntry submit",
                    "t ${entry.timestamp} s ${entry.sugarLevel} w ${entry.weight} e ${entry.extra} f ${entry.food} d ${entry.drink} tr ${entry.treatment} i ${entry.insulin}"
            )
            (activity as OnSugarEntryEnteredHandler).onSugarEntryEntered(entry)
        }
    }

    /**
     *  @doc Called by the main activity when the user changes the date.
     */
    fun handleDate(token: Int, year: Int, month: Int, day: Int) {
        when (token) {
            PickedType.start.ordinal -> {
                date.date = year to month to day
                val dateText = "%d-%02d-%02d".format(year, month, day)
                startDateView.text = dateText
            }
            PickedType.end.ordinal   -> {
                val date = endDate ?: DateHandler()
                date.date = year to month to day
                setEndTimes(date)
            }
        }
    }

    /**
     * @doc Called by the main activity when the user changes the time.
     */
    fun handleTime(token: Int, hour: Int, minute: Int) {
        when (token) {
            PickedType.start.ordinal -> {
                date.setTime(hour, minute)
                val timeText = "%02d:%02d".format(hour, minute)
                startTimeView.text = timeText
            }
            PickedType.end.ordinal   -> {
                val date = endDate ?: DateHandler()
                date.setTime(hour, minute)
                setEndTimes(date)
            }
        }
    }

    /**
     *
     */
    private fun setEndTimes(d: DateHandler?) {
        endDate = d
        endDate?.let {
            val dateText = "%d-%02d-%02d".format(it.year, it.month, it.day)
            val timeText = "%02d:%02d".format(it.hour, it.minute)
            endDateView.text = dateText
            endTimeView.text = timeText
        } ?: {
            endDateView.text = "-------"
            endTimeView.text = "-------"
        }()

    }

    companion object Creator {
        @JvmStatic
        fun newCreationInstance() = newCreationInstance(DateHandler().timestamp)

        @JvmStatic
        fun newCreationInstance(timestamp: Long): SugarEntryCreationActivity {
            val s = SugarEntryCreationActivity()
            val args = Bundle()
            d("SugarEntry creation", "Called with timestamp:$timestamp")
            args.putBoolean("EditCurrent", false)
            args.putLong("timestamp", timestamp)
            s.arguments = args
            return s
        }

        @JvmStatic
        fun newEditInstance(sugarEntry: SugarEntry): SugarEntryCreationActivity {
            val s = SugarEntryCreationActivity()
            val args = Bundle()
            d("SugarEntry edit", "Called with timestamp:${sugarEntry.timestamp}")
            args.putBoolean("EditCurrent", true)
            args.putParcelable("entry", sugarEntry)
            s.arguments = args
            return s
        }
    }
}

