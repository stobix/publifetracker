package stobix.app.lifetracker

import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker

import java.util.Calendar


class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    var token = 0
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val args=arguments ?: error("Null bundle")
        val hour = args.getInt("hour", c.get(Calendar.HOUR) )
        val minute = args.getInt("minute", c.get(Calendar.MINUTE))
        token = args.getInt("token",0)

        // Create a new instance of DatePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, true)
    }

    interface TimePickerHandler {
        fun handleTime(token: Int, hour: Int, minute: Int)
    }

    override fun onTimeSet(timePicker: TimePicker, hour: Int, minute: Int) {
        val timePickerHandler = activity as TimePickerHandler
        timePickerHandler.handleTime(token, hour, minute)
    }
}
