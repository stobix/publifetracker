package com.example.stobix.myapplication

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.NumberPicker

class NumberPickerFragment : DialogFragment(), NumberPickerDialog.OnNumberSetListener {
    override fun onNumberClear(view: NumberPickerDialog) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return NumberPickerDialog(activity, this, 4.2)
    }

    interface NumberPickerHandler {
        fun handleNumber(number: Float)
    }

    override fun onNumberSet(view: NumberPickerDialog, number: Float){
        val datePickerHandler = activity as NumberPickerHandler
        datePickerHandler.handleNumber(number)
    }
}
