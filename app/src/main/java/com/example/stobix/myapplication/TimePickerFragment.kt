package com.example.stobix.myapplication

import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker

import java.util.Calendar

/**
 * Created by stobix on 11/13/17.
 */

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of DatePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, true)
    }

    interface TimePickerHandler {
        fun handleTime(hour: Int, minute: Int)
    }

    override fun onTimeSet(timePicker: TimePicker, hour: Int, minute: Int) {
        val timePickerHandler = activity as TimePickerHandler
        timePickerHandler.handleTime(hour, minute)
    }
}
