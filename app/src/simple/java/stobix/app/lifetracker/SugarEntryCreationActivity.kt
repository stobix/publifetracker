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

/**
 * A dialog for creating an entry to the blood sugar database
 * Created by stobix on 11/19/17.
 */

typealias ShownList = List<View>
typealias HiddenList = List<View>
typealias ResourceID = Int

@Suppress("NAME_SHADOWING")
open class SugarEntryCreationActivity
@SuppressLint("ValidFragment") constructor
(
) : DialogFragment( )
{

    private var date: DateHandler = DateHandler()
    private var alreadyDefinedEntry: Boolean = false
    private var originalTimestamp: StartingTimestamp = 0
    private lateinit var dateView: TextView
    private lateinit var timeView: TextView
    private lateinit var sugarView: TextView
    private lateinit var weightView: TextView
    private lateinit var entry: SugarEntry
    private lateinit var foodView: TextView
    private lateinit var insulinView: TextView
    private lateinit var drinkView: TextView
    private lateinit var extraView: TextView
    private lateinit var pillsView: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alreadyDefinedEntry = arguments.getBoolean("EditCurrent")
        if(alreadyDefinedEntry) {
            entry=arguments.getParcelable("entry")
            date.timestamp= entry.timestamp
            originalTimestamp = entry.timestamp
            d( "SugarEntry create",
                    "already defined; timestamp:${entry.timestamp}, sugar: ${entry.sugarLevel}, extra: ${entry.timestamp}"
            )
        } else {
            date.timestamp = arguments.getLong("timestamp")
            d("SugarEntry create","creating new, timestamp:${date.timestamp}")
            entry = SugarEntry(timestamp= date.timestamp)
        }
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater?.inflate(R.layout.activity_sugar_entry_creation, container, false)

        v ?:throw Error("could not create view")

        dateView= v.findViewById(R.id.entryCreatorDate)
        timeView= v.findViewById(R.id.entryCreatorTime)
        sugarView= v.findViewById(R.id.entryCreatorSugar)
        weightView = v.findViewById(R.id.entryCreatorWeight)
        foodView = v.findViewById(R.id.entryCreatorFood)
        insulinView = v.findViewById(R.id.entryCreatorInsulin)
        pillsView = v.findViewById(R.id.entryCreatorPills)
        drinkView = v.findViewById(R.id.entryCreatorDrink)
        extraView = v.findViewById(R.id.entryCreatorExtra)

        val stateArray: MutableMap<Int,Boolean> = mutableMapOf()


        // Simulates a quick click on a component, e.g. for giving focus to a text field.
        fun View.click() {
            this.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0f, 0f, 0))
            this.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0f, 0f, 0))
        }



        fun vis(b: Boolean) =
                when (b){
                    true -> View.VISIBLE
                    false -> View.GONE
                }


        fun View.toggleBackgroundImage(b: Boolean){
            this.setBackgroundResource(if(b) R.drawable.icon_back_circle else R.drawable.icon_back_circle_inactive)
        }


        // Make the resource run the listener function on each click on the resource, with a boolean toggled each time.
        infix fun<A> Pair<ResourceID,A?>.togglingWithFun(listener: (Boolean) -> Unit) {
            val view = v.findViewById<ImageView>( first)
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
        infix fun<A> Pair<ResourceID,A?>.togglingWithoutFocus(shownHidden:Pair<ShownList,HiddenList>)  {
            val (shown,hidden) = shownHidden

            this togglingWithFun {
                val hiddenState = vis(it)
                val notHiddenState = vis(!it)
                shown.forEach { it.visibility = hiddenState }
                hidden.forEach { it.visibility = notHiddenState }
            }

        }

        // Stripped down versions

        infix fun<A> Pair<ResourceID,A?>.togglingWithoutFocus(shown:ShownList) = this togglingWithoutFocus (shown to emptyList())

        infix fun ResourceID.togglingWithoutFocus(shownHidden:Pair<ShownList,HiddenList>) = this to true togglingWithoutFocus shownHidden

        infix fun ResourceID.togglingWithoutFocus(shown:ShownList) = this to true togglingWithoutFocus shown


        // Toggles on a click on the Resource between showing the views in shown and hidden, giving focus to activated when shown list is shown (or first in shown list if activated is null)
        // Initial state shown is given by whether the property A is defined
        infix fun<A> Pair<ResourceID,A?>.toggling(shownHiddenActivated:Triple<ShownList,HiddenList,View?>)  {
            var (shown,hidden,activated) = shownHiddenActivated
            activated = activated ?:shown.first()

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
        infix fun<A> Pair<ResourceID,A?>.toggling(p:Pair<ShownList,HiddenList>) : Unit =
                this toggling (p to null)

        // Default to set focus to first in shown list, have nothing in hidden list
        infix fun<A> Pair<ResourceID,A?>.toggling(shown:ShownList): Unit  =
                this toggling (shown to emptyList())

        // Just declarative syntax sugar, kinda like => in perl.
        infix fun View.withLabel(id: ResourceID) = listOf(this,v.findViewById<View>(id))




        R.id.entryCreatorToggleDateTime togglingWithoutFocus (
                (dateView withLabel R.id.entryCreatorDateLabel) + (timeView withLabel R.id.entryCreatorTimeLabel)
                )

        R.id.entryCreatorWeightToggle to entry.weight toggling ( weightView withLabel R.id.entryCreatorWeightLabel )

        R.id.entryCreatorDrinkToggle to entry.drink toggling ( drinkView withLabel R.id.entryCreatorDrinkLabel )

        R.id.entryCreatorFoodToggle to entry.food toggling ( foodView withLabel R.id.entryCreatorFoodLabel )

        R.id.entryCreatorInsulinToggle to entry.insulin toggling (insulinView withLabel R.id.entryCreatorInsulinLabel )

        R.id.entryCreatorPillsToggle to entry.treatment toggling ( pillsView withLabel R.id.entryCreatorPillsLabel )

        R.id.entryCreatorSugarToggle to entry.sugarLevel toggling  ( sugarView withLabel R.id.entryCreatorSugarLabel )

        R.id.entryCreatorExtraToggle to entry.extra toggling  ( extraView withLabel R.id.entryCreatorExtraLabel )

        val dateText="%d-%02d-%02d".format(date.year,date.month+1,date.day)
        val timeText="%02d:%02d".format(date.hour,date.minute)
        dateView.text=dateText
        timeView.text=timeText

        val buttonAdd: Button = v.findViewById(R.id.entrySubmitAction2)
        val buttonAddClose: Button =v.findViewById(R.id.entrySubmitAction1)

        val buttonClearExtra: AppCompatImageView = v.findViewById(R.id.entryCreatorExtraDelete)

        if(alreadyDefinedEntry) {
            sugarView.text = entry.sugarLevel?.toFloat()?.div(10f)?.toString()
            weightView.text = entry.weight?.toFloat()?.div(10f)?.toString()
            extraView.text = entry.extra
            foodView.text = entry.food
            insulinView.text = entry.insulin?.toString()
            pillsView.text = entry.treatment
            drinkView.text = entry.drink
            buttonAddClose.text=getString(R.string.edit_dialog_button_update)
            buttonAdd.text=getString(R.string.edit_dialog_button_delete)
        } else {
            buttonAddClose.text=getString(R.string.creation_dialog_button_add_close)
            buttonAdd.visibility=View.VISIBLE
        }

        dateView.setOnClickListener { (activity as MainActivity).showDatePicker(date.year,date.month,date.day) }
        timeView.setOnClickListener { (activity as MainActivity).showTimePicker(date.hour,date.minute) }

        buttonAdd.setOnClickListener { onSubmit() }
        buttonAddClose.setOnClickListener { onSubmitAndClose() }
        buttonClearExtra.setOnClickListener { extraView.text="" }

        return v

    }


    // Sending a full SugarEntry since I'm not sure what fields it will contain in the future.
    interface OnSugarEntryEnteredHandler {
        fun onSugarEntryEntered(s: SugarEntry)
    }

    interface OnSugarEntryChangedHandler{
        fun onSugarEntryChanged(s: SugarEntry, originalTimestamp: Long)
    }

    interface OnSugarEntryDeletedHandler{
        fun onSugarEntryDeleted(s: SugarEntry)
    }

    private fun onSubmit() {
        if(alreadyDefinedEntry) {
            // delete the thing
            (activity as OnSugarEntryDeletedHandler).onSugarEntryDeleted(entry)
            dismiss()
        } else {
            handleSubmission()
            // Ensure we don't enter two entries with the same timestamp
            entry= SugarEntry(timestamp = entry.timestamp+1)
        }
    }

    private fun onSubmitAndClose() {
        handleSubmission()
        this.dismiss()
    }

    private fun String?.nullIfEmpty() = if (this.isNullOrEmpty()) null else this

    // Handles the submission of the entered data, either creating a new SugarEntry or updating the existing one.
    private fun handleSubmission(){
        entry.timestamp=date.timestamp
        entry.sugarLevel = sugarView.text?.toString()?.toFloatOrNull()?.times(10)?.toInt()
        entry.weight = weightView.text?.toString()?.toFloatOrNull()?.times(10)?.toInt()
        entry.extra = extraView.text?.toString().nullIfEmpty()
        entry.insulin = insulinView.text?.toString()?.toDoubleOrNull()
        entry.treatment = pillsView.text?.toString().nullIfEmpty()
        entry.food = foodView.text?.toString().nullIfEmpty()
        entry.drink = drinkView.text?.toString().nullIfEmpty()
        if(alreadyDefinedEntry) {
            d("SugarEntry update", "t ${entry.timestamp} s ${entry.sugarLevel} w ${entry.weight} e ${entry.extra} f ${entry.food} d ${entry.drink} tr ${entry.treatment} i ${entry.insulin}")
            (activity as OnSugarEntryChangedHandler).onSugarEntryChanged(entry,originalTimestamp)
        } else {
            d("SugarEntry submit", "t ${entry.timestamp} s ${entry.sugarLevel} w ${entry.weight} e ${entry.extra} f ${entry.food} d ${entry.drink} tr ${entry.treatment} i ${entry.insulin}")
            (activity as OnSugarEntryEnteredHandler).onSugarEntryEntered(entry)
        }
    }

    // Called by the main activity when the user changes the date.
    fun handleDate(year: Int, month: Int, day: Int) {
        date.setDate(year,month,day)
        // Calendars use a 0-indexed gregorian/julian month for some reason!
        val dateText="%d-%02d-%02d".format(year,month+1,day)
        dateView.text=dateText
    }

    // Called by the main activity when the user changes the time.
    fun handleTime(hour: Int, minute: Int) {
        date.setTime(hour,minute)
        val timeText= "%02d:%02d".format(hour,minute)
        timeView.text=timeText
    }

    companion object Creator{
        @JvmStatic fun newInstance() = newInstance(DateHandler().timestamp)
        @JvmStatic fun newInstance(timestamp: Long ): SugarEntryCreationActivity {
            val s = SugarEntryCreationActivity()
            val args = Bundle()
            d("SugarEntry creation", "Called with timestamp:$timestamp")
            args.putBoolean("EditCurrent",false)
            args.putLong("timestamp",timestamp)
            s.arguments=args
            return s
        }
        @JvmStatic fun newInstance(sugarEntry: SugarEntry): SugarEntryCreationActivity {
            val s = SugarEntryCreationActivity()
            val args = Bundle()
            d("SugarEntry edit", "Called with timestamp:${sugarEntry.timestamp}")
            args.putBoolean("EditCurrent",true)
            args.putParcelable("entry",sugarEntry)
            s.arguments=args
            return s
        }
    }
}

