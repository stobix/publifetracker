package com.example.stobix.myapplication

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.NumberPicker

import java.util.Calendar

class NumberPickerFragment : DialogFragment(), NumberPickerDialog.OnNumberSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return NumberPickerDialog(activity, this, 4.2)
    }

    interface NumberPickerHandler {
        fun handleNumber(number: Float)
    }

    override fun onNumberSet(view: NumberPicker, number: Float){
        val datePickerHandler = activity as NumberPickerHandler
        datePickerHandler.handleNumber(number)
    }
}
