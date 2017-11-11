package com.example.stobix.myapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by stobix on 9/14/17.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public interface ClickedListener {
        public void gotTheStuff(int year,int month,int day);
    }

    ClickedListener c;

    @Override
    public void onAttach(Activity a){
        super.onAttach(a);
        try{
            c = (ClickedListener) a;
        }  catch (ClassCastException e){
            throw new ClassCastException("the revolution has not yet happened");
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        c.gotTheStuff(year,month,day);
        // Do something with the date chosen by the user
    }
}
