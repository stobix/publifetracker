package com.example.stobix.myapplication

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment

class NumberPickerFragment : DialogFragment(), NumberPickerDialog.OnNumberSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val value = arguments.getInt("value",4)
        val fraction = arguments.getInt("fraction",2)
        val min = arguments.getInt("min",0)
        val max = arguments.getInt("max",100)
        return NumberPickerDialog(activity, this, value,fraction,min,max)
    }

    interface NumberPickedHandler {
        fun handleNumber(number: Int,fraction: Int)
    }

    interface NumberClearedHandler {
        fun handleNumberClear()
    }

    override fun onNumberSet(view: NumberPickerDialog, number: Int, fraction: Int){
        val numberPickedHandler = activity as NumberPickedHandler
        numberPickedHandler.handleNumber(number,fraction)
    }
    override fun onNumberClear(view: NumberPickerDialog) {
        val numberClearedHandler = activity as NumberClearedHandler
        numberClearedHandler.handleNumberClear()
    }
}
