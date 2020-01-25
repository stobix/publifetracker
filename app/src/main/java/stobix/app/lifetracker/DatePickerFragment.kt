package stobix.app.lifetracker

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker
import stobix.utils.DateHandler
import stobix.utils.kotlinExtensions.to

import java.util.Calendar

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var token = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val args= arguments ?: error("Null bundle")
        val date: DateHandler = args.getParcelable("date") ?: DateHandler()
        token = args.getInt("token",0)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, this, date.year, date.month0, date.day)
    }

    interface DatePickerHandler {
        fun handleDate(token: Int, date: DateHandler)
    }

    override fun onDateSet(view: DatePicker, year: Int, month0: Int, day: Int) {
        val datePickerHandler = activity as DatePickerHandler
        val date = DateHandler()
        date.date = year to (month0+1) to day
        datePickerHandler.handleDate(token, date)
    }
}
