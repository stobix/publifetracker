package com.example.stobix.myapplication

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker

import java.util.Calendar

/**
 * Created by stobix on 9/14/17.
 */

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, this, year, month, day)
    }

    interface DatePickerHandler {
        fun handleDate(year: Int, month: Int, day: Int)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val datePickerHandler = activity as DatePickerHandler
        datePickerHandler.handleDate(year, month, day)
    }
}
