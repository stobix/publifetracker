package com.example.stobix.myapplication;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by stobix on 11/13/17.
 */

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of DatePickerDialog and return it
            return new TimePickerDialog(getActivity(),this,hour,minute,true);
        }

    public interface TimePickerHandler {
            void handleTime(int hour,int minute);
        }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            TimePickerHandler timePickerHandler = (TimePickerHandler) getActivity();
            timePickerHandler.handleTime(hour,minute);
    }
}
