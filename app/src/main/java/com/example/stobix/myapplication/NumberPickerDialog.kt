package com.example.stobix.myapplication

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener

class NumberPickerDialog (
        val ctx: Context,
        val listener: OnNumberSetListener?,
        val defaultVal: String = "4.2",
        val minVal: String = "0.0",
        val maxVal: String = "100.0"
) :
        Dialog(ctx),
        OnValueChangeListener
{

    constructor(ctx: Context,listener: OnNumberSetListener?) :
            this(ctx,listener,"0.0","0.0","100.0")

    constructor(ctx: Context,
                listener: OnNumberSetListener?,
                default: Double = 0.0,
                min: Double = 0.0,
                max: Double = 100.0) :
            this(ctx,listener,default.toString(),min.toString(),max.toString())

    override fun show() {
        super.show()
        this.setTitle("testeli")
        this.setContentView(R.layout.number_picker_dialog)
        val ok: Button = this.findViewById(R.id.numberPickerOkButton)
        val ook: Button = this.findViewById(R.id.numberPickerCancelButton)
        val heltalspicker: NumberPicker = this.findViewById(R.id.numberPickerHeltal)
        heltalspicker.maxValue =  maxVal.substringBefore(".").toInt()
        heltalspicker.minValue = minVal.substringBefore(".").toInt()
        heltalspicker.value = defaultVal.substringBefore(".").toInt()
        heltalspicker.wrapSelectorWheel = false

        val decimalpicker: NumberPicker = this.findViewById(R.id.numberPickerDecimal)
        decimalpicker.maxValue =  9
        decimalpicker.minValue = 0
        decimalpicker.value = defaultVal.substringAfter(".").toInt()*10
        decimalpicker.wrapSelectorWheel = false

        ok.setOnClickListener {
            val n : Float = (heltalspicker.value.toDouble()+decimalpicker.value.toDouble()/10.0).toFloat()
            listener?.onNumberSet(this,n)
            this.dismiss()
        }
        ook.setOnClickListener {
            this.dismiss()
        }

    }
    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    interface OnNumberSetListener{
        fun onNumberSet(view: NumberPickerDialog, number: Float)

    }
}