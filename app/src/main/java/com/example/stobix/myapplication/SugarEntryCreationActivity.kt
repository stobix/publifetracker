package com.example.stobix.myapplication

import android.app.DialogFragment
import android.app.FragmentManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import java.util.*

/**
 * A dialog for creating an entry to the blood sugar database
 * Created by stobix on 11/19/17.
 */

class SugarEntryCreationActivity() : DialogFragment(), DatePickerFragment.DatePickerHandler, TimePickerFragment.TimePickerHandler {


    private var entry: SugarEntry = SugarEntry()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        entry = SugarEntry()
        entry.uid=arguments.getInt("uid")
        entry.epochTimestamp=arguments.getLong("timestamp")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater?.inflate(R.layout.activity_sugar_entry_creation, container, false)
        v ?: throw Error("could not create view")
        val dateV: TextView= v.findViewById(R.id.entryCreatorDate)
        val timeV: TextView= v.findViewById(R.id.entryCreatorTime)
        val date = Date(entry.epochTimestamp)
        dateV.text=""+date.year+"-"+date.month+"-"+date.day
        timeV.text=""+date.hours+":"+date.minutes+":"+date.seconds

        val closeButton: Button = v.findViewById(R.id.entryCreatorCloseButton)
        val addCloseButton: Button = v.findViewById(R.id.entryCreatorAddCloseButton)
        closeButton.setOnClickListener { (activity as MainActivity).closeEnterer() }
        addCloseButton.setOnClickListener { (activity as MainActivity).closeEnterer() }

        return v
    }

    /*
    fun showDatePicker(view: View) {
        DatePickerFragment().show(childFragmentManager, "datePicker");
    }

    fun showTimePicker(view: View) {
        TimePickerFragment().show(fragmentManager, "timePicker");
    }
    */

    // Sending a full SugarEntry since I'm not sure what fields it will contain in the future.
    interface OnSugarEntryEnteredHandler {
        fun onSugarEntryEntered(s: SugarEntry);
    }

    fun onSubmit(){
        TODO("Check entry, Submit entry, Create new entry")
        //val onSugarEntryHandler = activity as OnSugarEntryEnteredHandler
        //onSugarEntryHandler.onSugarEntryEntered(entry)
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

    companion object Create {
        fun newInstance(uid: Int, timestamp: Long = Date().time) : SugarEntryCreationActivity{
            val s = SugarEntryCreationActivity()
            val args = Bundle()
            d("SugarEntry(Create)","Called with uid:"+uid+" timestamp:"+timestamp)
            args.putInt("uid",uid)
            args.putLong("timestamp",timestamp)
            s.arguments=args
            return s
        }
    }
}

