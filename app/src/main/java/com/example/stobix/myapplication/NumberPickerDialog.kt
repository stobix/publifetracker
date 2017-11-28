package com.example.stobix.myapplication

import android.app.AlertDialog
import android.content.Context
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener

class NumberPickerDialog (
        val ctx: Context,
        val listener: OnNumberSetListener?,
        val defaultVal: Double = 0.0,
        val minVal: Double = 0.0,
        val maxVal: Double = 100.0
) :
        AlertDialog(ctx),
        OnValueChangeListener
{
    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    interface OnNumberSetListener{
        fun onNumberSet(view: NumberPicker, number: Float)

    }
}