package com.example.stobix.myapplication

import android.annotation.SuppressLint
import android.app.DialogFragment
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

/**
 * A dialog for creating an entry to the blood sugar database
 * Created by stobix on 11/19/17.
 */

open class SugarEntryCreationActivity
@SuppressLint("ValidFragment") constructor
(
) : DialogFragment( )
{

    private var uid: Int=0
    private var date: DateHandler = DateHandler()
    private var sugarLevel: Double? = null
    private var alreadyDefinedEntry: Boolean = false
    lateinit private var dateView: TextView
    lateinit private var timeView: TextView
    lateinit private var sugarView: TextView
    lateinit private var entry: SugarEntry


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alreadyDefinedEntry = arguments.getBoolean("EditCurrent")
        if(alreadyDefinedEntry) {
            entry=arguments.getParcelable("entry")
            uid= entry.uid
            date.timestamp= entry.epochTimestamp
            sugarLevel=if (entry.sugarLevel==-1) null else entry.sugarLevel.toDouble()/10f
            d( "SugarEntry create",
                    "already defined, uid:${entry.uid}, timestamp:${entry.epochTimestamp}, sugar: ${entry.sugarLevel}, extra: ${entry.epochTimestamp}"
            )
        } else {
            uid = arguments.getInt("uid")
            date.timestamp = arguments.getLong("timestamp")
            d("SugarEntry create","creating new, uid:${uid}, timestamp:${date.timestamp}")
            entry = SugarEntry(uid, date.timestamp)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater?.inflate(R.layout.activity_sugar_entry_creation, container, false)

        v ?:throw Error("could not create view")

        dateView= v.findViewById(R.id.entryCreatorDate)
        timeView= v.findViewById(R.id.entryCreatorTime)
        sugarView= v.findViewById(R.id.entryCreatorSugar)
        val extraV = v.findViewById<TextView>(R.id.entryCreatorExtra)

        val dateText="%d-%02d-%02d".format(date.year,date.month+1,date.day)
        val timeText="%02d:%02d".format(date.hour,date.minute)
        dateView.text=dateText
        timeView.text=timeText

        val buttonAdd: Button = v.findViewById<Button>(R.id.entryAdd)
        val buttonAddClose: Button =v.findViewById<Button>(R.id.entryAddClose)

        if(alreadyDefinedEntry) {
            sugarView.text=sugarLevel?.toString()?:""
            extraV.text= entry.extra
            buttonAddClose.text=getString(R.string.edit_dialog_button_update)
            buttonAdd.visibility=View.GONE
        } else {
            buttonAddClose.text=getString(R.string.creation_dialog_button_add_close)
            buttonAdd.visibility=View.VISIBLE
        }

        dateView.setOnClickListener { (activity as MainActivity).showDatePicker(date.year,date.month,date.day) }
        timeView.setOnClickListener { (activity as MainActivity).showTimePicker(date.hour,date.minute) }
        sugarView.setOnClickListener{ (activity as MainActivity).showNumberPicker(sugarLevel?:4.2,0.0,100.0) }

        buttonAdd.setOnClickListener {onSubmit(extraV)}
        buttonAddClose.setOnClickListener { onSubmitAndClose(extraV) }

        return v

    }

    // Sending a full SugarEntry since I'm not sure what fields it will contain in the future.
    interface OnSugarEntryEnteredHandler {
        fun onSugarEntryEntered(s: SugarEntry)
    }

    interface OnSugarEntryChangedHandler{
        fun onSugarEntryChanged(s: SugarEntry)
    }

    private fun onSubmit(extraV: TextView) {
        handleSubmission(extraV)
        if(!alreadyDefinedEntry) {
            uid++
            entry=SugarEntry(uid)
        }
    }

    private fun onSubmitAndClose(extraView: TextView) {
        handleSubmission(extraView)
        this.dismiss()
    }

    private fun handleSubmission(extraView: TextView){
        entry.epochTimestamp=date.timestamp
        entry.sugarLevel = sugarLevel?.times(10)?.toInt() ?: -1
        entry.extra = extraView.text?.toString() ?: "N/A"
        if(alreadyDefinedEntry) {
            d("SugarEntry update", "" + entry.uid + " " + entry.epochTimestamp + " " + entry.sugarLevel + " " + entry.extra)
            (activity as OnSugarEntryChangedHandler).onSugarEntryChanged(entry)
        } else {
            d("SugarEntry submit", "" + entry.uid + " " + entry.epochTimestamp + " " + entry.sugarLevel + " " + entry.extra)
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

    fun onNumberSet(number: Float) {
        sugarLevel=number.toDouble()
        sugarView.text=number.toString()
    }

    fun onNumberClear() {
        sugarLevel=null
        sugarView.text=""
    }

    companion object Creator{
        @JvmStatic fun newInstance(uid: Int) = newInstance(uid,DateHandler().timestamp)
        @JvmStatic fun newInstance(uid: Int, timestamp: Long ): SugarEntryCreationActivity{
            val s = SugarEntryCreationActivity()
            val args = Bundle()
            d("SugarEntry creation","Called with uid:"+uid+" timestamp:"+timestamp)
            args.putBoolean("EditCurrent",false)
            args.putInt("uid",uid)
            args.putLong("timestamp",timestamp)
            s.arguments=args
            return s
        }
        @JvmStatic fun newInstance(sugarEntry: SugarEntry): SugarEntryCreationActivity{
            val s = SugarEntryCreationActivity()
            val args = Bundle()
            d("SugarEntry edit","Called with uid:"+sugarEntry.uid+" timestamp:"+sugarEntry.epochTimestamp)
            args.putBoolean("EditCurrent",true)
            args.putParcelable("entry",sugarEntry)
            s.arguments=args
            return s
        }
    }
}

