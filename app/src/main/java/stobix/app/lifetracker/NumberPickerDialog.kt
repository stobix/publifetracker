package stobix.app.lifetracker

import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener

class NumberPickerDialog (
        ctx: Context,
        val listener: OnNumberSetListener?,
        val values: Pair<Int,Int> = Pair(4,2),
        val minVal: Int = 0,
        val maxVal: Int = 100
) :
        Dialog(ctx),
        OnValueChangeListener
{

    override fun show() {
        super.show()
        //this.setTitle(context.getString(R.string.number_picker_sugar_level))
        this.setContentView(R.layout.number_picker_dialog)

        val (heltalsdel,decimaldel)=values
        val okButton: Button = this.findViewById(R.id.numberPickerOkButton)
        val clearButton: Button = this.findViewById(R.id.numberPickerClearButton)
        val heltalspicker: NumberPicker = this.findViewById(R.id.numberPickerHeltal)
        heltalspicker.maxValue =  maxVal
        heltalspicker.minValue = minVal
        heltalspicker.value = heltalsdel
        heltalspicker.wrapSelectorWheel = false

        val decimalpicker: NumberPicker = this.findViewById(R.id.numberPickerDecimal)
        decimalpicker.maxValue =  9
        decimalpicker.minValue = 0
        decimalpicker.value = decimaldel
        decimalpicker.wrapSelectorWheel = false

        okButton.setOnClickListener {
            listener?.onNumberSet(this,Pair(heltalspicker.value,decimalpicker.value))
            this.dismiss()
        }

        clearButton.setOnClickListener {
            listener?.onNumberClear(this)
            this.dismiss()
        }

    }
    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    interface OnNumberSetListener{
        fun onNumberSet(view: NumberPickerDialog, value: Pair<Int,Int>)
        fun onNumberClear(view: NumberPickerDialog)
    }
}