package com.example.stobix.myapplication

import android.annotation.SuppressLint
import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.*

/**
 * A dialog for creating an entry to the blood sugar database
 * Created by stobix on 11/19/17.
 */

class SugarEntryCreationActivity
@SuppressLint("ValidFragment") constructor
(
        var entry: SugarEntry = SugarEntry()
) : DialogFragment( ), DatePickerFragment.DatePickerHandler, TimePickerFragment.TimePickerHandler {

    //constructor() :  this(SugarEntry())




    override fun onCreate(savedInstanceState: Bundle?) {
        d("SugarEntry create","weeeee");
        super.onCreate(savedInstanceState)
        entry.uid = arguments.getInt("uid")
        entry.epochTimestamp = arguments.getLong("timestamp")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater?.inflate(R.layout.activity_sugar_entry_creation, container, false)
        if (v != null) {
            val dateV: TextView= v.findViewById(R.id.entryCreatorDate)
            val timeV: TextView= v.findViewById(R.id.entryCreatorTime)
            val date = Date(entry.epochTimestamp)
            val dateText=""+(1900+date.year)+"/"+date.month+"/"+date.day
            val timeText=""+date.hours+":"+date.minutes+":"+date.seconds
            dateV.text=dateText
            timeV.text=timeText
            return v
        } else throw Error("could not create view")
    }

    // Sending a full SugarEntry since I'm not sure what fields it will contain in the future.
    interface OnSugarEntryEnteredHandler {
        fun onSugarEntryEntered(s: SugarEntry);
    }

    fun onSubmit(v: View){
        TODO("Check entry, Submit entry, Create new entry")

        val onSugarEntryHandler = activity as OnSugarEntryEnteredHandler
        onSugarEntryHandler.onSugarEntryEntered(entry)
    }

    fun onSubmitAndClose(){
        TODO("Check entry, Submit entry, Close view")

    }

    fun onClose(){
        TODO("Forfeit entry, Close view")

    }

    override fun handleDate(year: Int, month: Int, day: Int) {
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
        fun newInstance(uid: Int) = newInstance(uid,Date().time)
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

