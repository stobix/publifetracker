package stobix.app.lifetracker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment

class NumberPickerFragment : DialogFragment(), NumberPickerDialog.OnNumberSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args=arguments ?: error("Null bundle")
        val value = args.getInt("value",4)
        val fraction = args.getInt("fraction",2)
        val min = args.getInt("min",0)
        val max = args.getInt("max",100)
        return NumberPickerDialog(activity as Context, this, Pair(value, fraction), min, max)
    }

    interface NumberPickedHandler {
        fun handleNumber(number: Pair<Int, Int>)
    }

    interface NumberClearedHandler {
        fun handleNumberClear()
    }

    override fun onNumberSet(view: NumberPickerDialog, value: Pair<Int,Int>){
        val numberPickedHandler = activity as NumberPickedHandler
        numberPickedHandler.handleNumber(value)
    }
    override fun onNumberClear(view: NumberPickerDialog) {
        val numberClearedHandler = activity as NumberClearedHandler
        numberClearedHandler.handleNumberClear()
    }
}
