package stobix.app.lifetracker

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.*
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener

// A dialog to pick an xx…x.y number using dials.

class NumberPickerDialog (
        ctx: Context,
        val listener: OnNumberSetListener?,
        values: Pair<Int,Int> = Pair(4,2),
        minVal: Int = 0,
        maxVal: Int = 100
) :
        AlertDialog(ctx),
        OnValueChangeListener,
        DialogInterface.OnClickListener
{

    val heltalspicker: NumberPicker
    val decimalpicker: NumberPicker

    init {
        // This mostly echoes what is done in TimePickerDialog
        val inflater =LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.number_picker_dialog,null)
        setView(view)

        setButton(BUTTON_POSITIVE,
                context.getText(R.string.number_picker_ok_button),this)
        setButton(BUTTON_NEUTRAL,
                context.getText(R.string.number_picker_clear_button),this)
        setButton(BUTTON_NEGATIVE,
                context.getString(R.string.number_picker_cancel_button),this)

        val (heltalsdel,decimaldel)=values

        heltalspicker = view.findViewById(R.id.numberPickerHeltal)
        decimalpicker = view.findViewById(R.id.numberPickerDecimal)

        heltalspicker.maxValue =  maxVal
        heltalspicker.minValue = minVal
        heltalspicker.value = heltalsdel
        heltalspicker.wrapSelectorWheel = false

        decimalpicker.maxValue =  9
        decimalpicker.minValue = 0
        decimalpicker.value = decimaldel
        decimalpicker.wrapSelectorWheel = false

        // This does not seem to work for API 22, and is useless for newer ones.
        // window.setLayout( WRAP_CONTENT, WRAP_CONTENT)
    }


    override fun show() {
        super.show()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when(which){
            BUTTON_POSITIVE -> {
                listener?.onNumberSet(this,Pair(heltalspicker.value,decimalpicker.value))
                this.dismiss()
            }

            BUTTON_NEUTRAL -> {
                listener?.onNumberClear(this)
                this.dismiss()
            }

            BUTTON_NEGATIVE -> cancel()

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