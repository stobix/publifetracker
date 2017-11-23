package com.example.stobix.myapplication

import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*

/**
 * Created by stobix on 11/19/17.
 */

class SugarEntryCreationActivity() : DialogFragment()//, DatePickerFragment.DatePickerHandler, TimePickerFragment.TimePickerHandler
{

    private var entry: SugarEntry

    init {
        // This will have a bogus index lest I do something stupid like provide the correct index in the constructor
        // This might be good though; It lets the main activity always know which index this view think it has
        // OTOH it creates unnecessary dependencies
        entry = SugarEntry()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater?.inflate(R.layout.activity_sugar_entry_creation, container, false)
        return v ?: super.onCreateView(inflater, container, savedInstanceState)
    }


    }
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

/*
    override fun handleDate(year: Int, month: Int, day: Int) {
        TODO("Add date to entry") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleTime(hour: Int, minute: Int) {
        TODO("Add time to entry") //To change body of created functions use File | Settings | File Templates.
    }
*/

    // fun handleOtherStuff
    // fun genericHandler(int value, string which)
    // fun genericHandler(string value, string which)
    // ...

}
