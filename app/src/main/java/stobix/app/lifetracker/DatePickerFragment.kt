package stobix.app.lifetracker

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker

import java.util.Calendar

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val args=arguments ?: error("Null bundle")
        val year = args.getInt("year",c.get(Calendar.YEAR))
        val month = args.getInt("month", c.get(Calendar.MONTH))
        val day = args.getInt("day", c.get(Calendar.DAY_OF_MONTH))

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
