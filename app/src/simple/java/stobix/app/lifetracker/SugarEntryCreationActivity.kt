package stobix.app.lifetracker

import android.annotation.SuppressLint
import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import stobix.utils.DateHandler

/**
 * A dialog for creating an entry to the blood sugar database
 * Created by stobix on 11/19/17.
 */

open class SugarEntryCreationActivity
@SuppressLint("ValidFragment") constructor
(
) : DialogFragment( )
{

    private var date: DateHandler = DateHandler()
    private var sugarLevel: Int? = null
    private var weight: Int? = null
    private var alreadyDefinedEntry: Boolean = false
    lateinit private var dateView: TextView
    lateinit private var timeView: TextView
    lateinit private var sugarView: TextView
    lateinit private var weightView: TextView
    lateinit private var entry: SugarEntry


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alreadyDefinedEntry = arguments.getBoolean("EditCurrent")
        if(alreadyDefinedEntry) {
            entry=arguments.getParcelable("entry")
            date.timestamp= entry.epochTimestamp
            weight = entry.weight
            sugarLevel=entry.sugarLevel
            d( "SugarEntry create",
                    "already defined; timestamp:${entry.epochTimestamp}, sugar: ${entry.sugarLevel}, extra: ${entry.epochTimestamp}"
            )
        } else {
            date.timestamp = arguments.getLong("timestamp")
            d("SugarEntry create","creating new, timestamp:${date.timestamp}")
            entry = SugarEntry(epochTimestamp= date.timestamp)
        }
    }

    private fun sugarLevelToString(): String {
        val sugarLevel=sugarLevel
        when {
            sugarLevel == null -> return ""
            else -> return "${sugarLevel / 10}.${sugarLevel % 10}"
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater?.inflate(R.layout.activity_sugar_entry_creation, container, false)

        v ?:throw Error("could not create view")

        dateView= v.findViewById(R.id.entryCreatorDate)
        timeView= v.findViewById(R.id.entryCreatorTime)
        sugarView= v.findViewById(R.id.entryCreatorSugar)
        weightView = v.findViewById(R.id.entryCreatorWeight)
        val extraV = v.findViewById<TextView>(R.id.entryCreatorExtra)

        val dateText="%d-%02d-%02d".format(date.year,date.month+1,date.day)
        val timeText="%02d:%02d".format(date.hour,date.minute)
        dateView.text=dateText
        timeView.text=timeText
        weightView.text=weight?.toFloat()?.div(10f)?.toString() ?: ""

        val buttonAdd: Button = v.findViewById<Button>(R.id.entryAdd)
        val buttonAddClose: Button =v.findViewById<Button>(R.id.entryAddClose)

        val buttonClearExtra: AppCompatImageView = v.findViewById(R.id.entryCreatorExtraDelete)

        if(alreadyDefinedEntry) {
            sugarView.text=sugarLevelToString()
            extraV.text= entry.extra
            buttonAddClose.text=getString(R.string.edit_dialog_button_update)
            buttonAdd.text=getString(R.string.edit_dialog_button_delete)
        } else {
            buttonAddClose.text=getString(R.string.creation_dialog_button_add_close)
            buttonAdd.visibility=View.VISIBLE
        }

        dateView.setOnClickListener { (activity as MainActivity).showDatePicker(date.year,date.month,date.day) }
        timeView.setOnClickListener { (activity as MainActivity).showTimePicker(date.hour,date.minute) }

        buttonAdd.setOnClickListener { onSubmit(extraV) }
        buttonAddClose.setOnClickListener { onSubmitAndClose(extraV) }
        buttonClearExtra.setOnClickListener { extraV.text="" }

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

    private fun onSubmit(extraV: TextView) {
        if(alreadyDefinedEntry) {
            // delete the thing
            (activity as OnSugarEntryDeletedHandler).onSugarEntryDeleted(entry)
            dismiss()
        } else {
            handleSubmission(extraV)
            // Ensure we don't enter two entries with the same timestamp
            entry= SugarEntry(epochTimestamp = entry.epochTimestamp+1)
        }
    }

    private fun onSubmitAndClose(extraView: TextView) {
        handleSubmission(extraView)
        this.dismiss()
    }

    private fun handleSubmission(extraView: TextView){
        entry.epochTimestamp=date.timestamp
        entry.sugarLevel = sugarView.text?.toString()?.toFloatOrNull()?.times(10)?.toInt()
        entry.weight = weightView.text?.toString()?.toFloatOrNull()?.times(10)?.toInt()
        entry.extra = extraView.text?.toString() ?: "N/A"
        if(alreadyDefinedEntry) {
            d("SugarEntry update", "${entry.epochTimestamp} ${entry.sugarLevel} ${entry.weight} ${entry.extra}")
            (activity as OnSugarEntryChangedHandler).onSugarEntryChanged(entry)
        } else {
            d("SugarEntry submit", "${entry.epochTimestamp} ${entry.sugarLevel} ${entry.weight} ${entry.extra}")
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
            d("SugarEntry creation","Called with timestamp:"+timestamp)
            args.putBoolean("EditCurrent",false)
            args.putLong("timestamp",timestamp)
            s.arguments=args
            return s
        }
        @JvmStatic fun newInstance(sugarEntry: SugarEntry): SugarEntryCreationActivity {
            val s = SugarEntryCreationActivity()
            val args = Bundle()
            d("SugarEntry edit","Called with timestamp:"+sugarEntry.epochTimestamp)
            args.putBoolean("EditCurrent",true)
            args.putParcelable("entry",sugarEntry)
            s.arguments=args
            return s
        }
    }
}

