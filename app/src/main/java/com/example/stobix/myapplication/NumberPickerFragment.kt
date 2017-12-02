package com.example.stobix.myapplication

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.NumberPicker

class NumberPickerFragment : DialogFragment(), NumberPickerDialog.OnNumberSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val value = arguments.getDouble("value",4.2)
        val min = arguments.getDouble("min",0.0)
        val max = arguments.getDouble("max",100.0)
        return NumberPickerDialog(activity, this, value,min,max)
    }

    interface NumberPickedHandler {
        fun handleNumber(number: Float)
    }

    interface NumberClearedHandler {
        fun handleNumberClear()
    }

    override fun onNumberSet(view: NumberPickerDialog, number: Float){
        val numberPickedHandler = activity as NumberPickedHandler
        numberPickedHandler.handleNumber(number)
    }
    override fun onNumberClear(view: NumberPickerDialog) {
        val numberClearedHandler = activity as NumberClearedHandler
        numberClearedHandler.handleNumberClear()
    }
}
