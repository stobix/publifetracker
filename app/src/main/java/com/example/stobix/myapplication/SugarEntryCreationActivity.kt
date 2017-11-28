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
) : DialogFragment( ), DatePickerFragment.DatePickerHandler, TimePickerFragment.TimePickerHandler {

    private var uid: Int=0
    private var date: DateHandler= DateHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        d("SugarEntry create","weeeee")
        super.onCreate(savedInstanceState)
        uid = arguments.getInt("uid")
        date.timestamp = arguments.getLong("timestamp")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        d("SugarEntry view","weeeee")
        val v = inflater?.inflate(R.layout.activity_sugar_entry_creation, container, false)

        v ?:throw Error("could not create view")

        val dateV: TextView= v.findViewById(R.id.entryCreatorDate)
        val timeV: TextView= v.findViewById(R.id.entryCreatorTime)

        val dateText=""+date.year+"-"+date.month+"-"+date.day
        val timeText=""+date.hour+":"+date.minute // +s":"+date.second
        dateV.text=dateText
        timeV.text=timeText


        val sugarV = v.findViewById<TextView>(R.id.entryCreatorSugar)
        val extraV = v.findViewById<TextView>(R.id.entryCreatorExtra)

        v.findViewById<Button>(R.id.entryAdd).setOnClickListener {onSubmit(dateV,timeV,sugarV,extraV)}
        v.findViewById<Button>(R.id.entryClose).setOnClickListener { onClose() }
        v.findViewById<Button>(R.id.entryAddClose).setOnClickListener { onSubmitAndClose(dateV,timeV,sugarV,extraV) }

        return v

    }

    // Sending a full SugarEntry since I'm not sure what fields it will contain in the future.
    interface OnSugarEntryEnteredHandler {
        fun onSugarEntryEntered(s: SugarEntry)
    }

    private fun onSubmit(dateV: TextView, timeV: TextView, sugarV: TextView, extraV: TextView) {
        handleSubmission(dateV, timeV, sugarV, extraV)
        uid++
    }

    private fun onSubmitAndClose(dateV: TextView, timeV: TextView, sugarV: TextView, extraV: TextView) {
        handleSubmission(dateV, timeV, sugarV, extraV)
    }

    private fun handleSubmission(dateV: TextView, timeV: TextView, sugarV: TextView, extraV: TextView){
        val entry= SugarEntry(uid,date.timestamp)
        entry.sugarLevel = try {
            (java.lang.Float.valueOf(sugarV.text?.toString()?:"0")*10).toInt()
        }
        catch (_: Exception) {
            0
        }
        entry.extra = extraV.text?.toString()?:"N/A"
        d("SugarEntry submit", ""+entry.uid+" "+entry.epochTimestamp+" "+entry.sugarLevel+" "+entry.extra)
        (activity as OnSugarEntryEnteredHandler).onSugarEntryEntered(entry)
    }

    private fun onClose(){
        TODO("Forfeit entry, Close view. Can I even do this?")
    }

    override fun handleDate(year: Int, month: Int, day: Int) {
        d("SugarEntry got date",""+year+"-"+month+"-"+day)
        TODO("Add date to entry")
    }

    override fun handleTime(hour: Int, minute: Int) {
        TODO("Add time to entry")
    }

    // fun handleOtherStuff
    // fun genericHandler(int value, string which)
    // fun genericHandler(string value, string which)
    // ...

    companion object{
        fun newInstance(uid: Int) = newInstance(uid,DateHandler().timestamp)
        fun newInstance(uid: Int, timestamp: Long ) : SugarEntryCreationActivity{
            val s = SugarEntryCreationActivity()
            val args = Bundle()
            d("EntryCreation","Called with uid:"+uid+" timestamp:"+timestamp)
            args.putInt("uid",uid)
            args.putLong("timestamp",timestamp)
            s.arguments=args
            return s
        }
    }
}

