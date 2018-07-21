package stobix.app.lifetracker

import android.annotation.SuppressLint
import android.app.DialogFragment
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import stobix.utils.DateHandler

/**
 * A dialog for creating an entry to the blood sugar database
 * Created by stobix on 11/19/17.
 */

@Suppress("NAME_SHADOWING")
open class SugarEntryCreationActivity
@SuppressLint("ValidFragment") constructor
(
) : DialogFragment( )
{

    private var date: DateHandler = DateHandler()
    private var alreadyDefinedEntry: Boolean = false
    private lateinit var dateView: TextView
    private lateinit var timeView: TextView
    private lateinit var sugarView: TextView
    private lateinit var weightView: TextView
    private lateinit var entry: SugarEntry
    private lateinit var foodView: TextView
    private lateinit var treatmentView: TextView
    private lateinit var drinkView: TextView
    private lateinit var extraView: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alreadyDefinedEntry = arguments.getBoolean("EditCurrent")
        if(alreadyDefinedEntry) {
            entry=arguments.getParcelable("entry")
            date.timestamp= entry.timestamp
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
        treatmentView = v.findViewById(R.id.entryCreatorTreatment)
        drinkView = v.findViewById(R.id.entryCreatorDrink)
        extraView = v.findViewById(R.id.entryCreatorExtra)

        val stateArray: MutableMap<Int,Boolean> = mutableMapOf()

        fun vis(b: Boolean) =
                when (b){
                    true -> View.VISIBLE
                    false -> View.GONE
                }

        infix fun<A> Pair<Int,A?>.viewListener(listener: (Int) -> Unit) {
            val view = v.findViewById<ImageView>( first)
            val truthiness = second != null
            val visibleValue = stateArray[first] ?: truthiness
            val hiddenState = vis(visibleValue)
            listener(hiddenState)
            view.setBackgroundColor(if(visibleValue) Color.BLACK else Color.WHITE)
            stateArray[this.first] = !visibleValue

            view.setOnClickListener {
                val visibleValue = stateArray[first] ?: truthiness
                val hiddenState = vis(visibleValue)
                it.setBackgroundColor(if(visibleValue) Color.BLACK else Color.WHITE)
                listener(hiddenState)
                stateArray[this.first] = !visibleValue
            }
        }


        infix fun<A> Pair<Int,A?>.elemHider(p:Pair<List<View>,List<View>>)  {
            val view = v.findViewById<ImageView>( first)
            val truthiness = second != null
            val visibleValue = stateArray[first] ?: truthiness
            val hiddenState = vis(visibleValue)
            val notHiddenState = vis(!visibleValue)
            val (shown,hidden) = p
            shown.forEach { it.visibility = hiddenState }
            hidden.forEach { it.visibility = notHiddenState }
            view.setBackgroundColor(if(visibleValue) Color.BLACK else Color.WHITE)
            stateArray[first] = visibleValue

            view.setOnClickListener {
                val visibleValue = stateArray[first] ?: truthiness
                val hiddenState = vis(visibleValue)
                val notHiddenState = vis(!visibleValue)
                it.setBackgroundColor(if(visibleValue) Color.BLACK else Color.WHITE)
                shown.map { it.visibility = hiddenState }
                hidden.map { it.visibility = notHiddenState }
                stateArray[first] = !visibleValue
            }

        }

       R.id.entryCreatorToggleDateTime to true viewListener {
            dateView.visibility  = it
            timeView.visibility = it
            v.findViewById<TextView>(R.id.entryCreatorDateLabel).visibility = it
            v.findViewById<TextView>(R.id.entryCreatorTimeLabel).visibility = it
        }

        R.id.entryCreatorWeightToggle to entry.weight elemHider(
                listOf( v.findViewById(R.id.entryCreatorWeightLabel), weightView)
                        to emptyList()
                )


        R.id.entryCreatorWeightToggle to entry.weight viewListener {
            v.findViewById<TextView>(R.id.entryCreatorWeightLabel).visibility = it
            weightView.visibility = it
        }

        R.id.entryCreatorDrinkToggle to entry.drink viewListener {
            v.findViewById<TextView>(R.id.entryCreatorDrinkLabel).visibility = it
            drinkView.visibility = it
        }

        R.id.entryCreatorFoodToggle to entry.food viewListener {
            v.findViewById<TextView>(R.id.entryCreatorFoodLabel).visibility = it
            foodView.visibility = it
        }

        R.id.entryCreatorTreatmentToggle to entry.treatment viewListener {
            v.findViewById<TextView>(R.id.entryCreatorTreatmentLabel).visibility = it
            treatmentView.visibility = it
        }

        R.id.entryCreatorSugarToggle to entry.sugarLevel viewListener  {
            v.findViewById<TextView>(R.id.entryCreatorSugarLabel).visibility = it
            sugarView.visibility = it
        }

        R.id.entryCreatorExtraToggle to entry.extra viewListener  {
            v.findViewById<TextView>(R.id.entryCreatorExtraLabel).visibility = it
            extraView.visibility = it
        }

        val dateText="%d-%02d-%02d".format(date.year,date.month+1,date.day)
        val timeText="%02d:%02d".format(date.hour,date.minute)
        dateView.text=dateText
        timeView.text=timeText

        val buttonAdd: Button = v.findViewById(R.id.entryAdd)
        val buttonAddClose: Button =v.findViewById(R.id.entryAddClose)

        val buttonClearExtra: AppCompatImageView = v.findViewById(R.id.entryCreatorExtraDelete)

        if(alreadyDefinedEntry) {
            sugarView.text = entry.sugarLevel?.toFloat()?.div(10f)?.toString()
            weightView.text = entry.weight?.toFloat()?.div(10f)?.toString()
            extraView.text = entry.extra
            foodView.text = entry.food
            treatmentView.text = entry.treatment
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
        fun onSugarEntryChanged(s: SugarEntry)
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

    private fun handleSubmission(){
        entry.timestamp=date.timestamp
        entry.sugarLevel = sugarView.text?.toString()?.toFloatOrNull()?.times(10)?.toInt()
        entry.weight = weightView.text?.toString()?.toFloatOrNull()?.times(10)?.toInt()
        entry.extra = extraView.text?.toString().nullIfEmpty()
        entry.treatment = treatmentView.text?.toString().nullIfEmpty()
        entry.food = foodView.text?.toString().nullIfEmpty()
        entry.drink = drinkView.text?.toString().nullIfEmpty()
        if(alreadyDefinedEntry) {
            d("SugarEntry update", "t ${entry.timestamp} s ${entry.sugarLevel} w ${entry.weight} e ${entry.extra} f ${entry.food} d ${entry.drink} tr ${entry.treatment}")
            (activity as OnSugarEntryChangedHandler).onSugarEntryChanged(entry)
        } else {
            d("SugarEntry submit", "t ${entry.timestamp} s ${entry.sugarLevel} w ${entry.weight} e ${entry.extra} f ${entry.food} d ${entry.drink} tr ${entry.treatment}")
            (activity as OnSugarEntryEnteredHandler).onSugarEntryEntered(entry)
        }
    }

    fun handleDate(year: Int, month: Int, day: Int) {
        date.setDate(year,month,day)
        // Calendars use a 0-indexed gregorian/julian month for some reason!
        val dateText="%d-%02d-%02d".format(year,month+1,day)
        dateView.text=dateText
    }

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

