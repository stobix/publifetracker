package com.example.stobix.myapplication;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import de.codecrafters.tableview.TableDataAdapter;

import static android.util.Log.d;
import static java.lang.String.format;

/**
 * Created by stobix on 11/11/17.
 */

public class SugarEntryTableDataAdapter extends TableDataAdapter<SugarEntry> {
    public SugarEntryTableDataAdapter(Context context, ArrayList<SugarEntry> entries) {
        this(context, (SugarEntry[])entries.toArray());
    }
    SugarEntryTableDataAdapter(Context context,SugarEntry[] entries){
        super(context,entries);
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        d("DB","Processing "+columnIndex+" "+rowIndex);
        SugarEntry currRow = getRowData(rowIndex);
        View renderedView = null;
        switch(columnIndex) {
            case 0:
                String formatString = getResources().getString(R.string.dateFormat);
                DateFormat df = new DateFormat();
                Date myDate=new Date(currRow.epochTimestamp);
                String myDateString = df.format(formatString,myDate).toString();
                renderedView = renderString(myDateString);
                d("DB render","0 "+rowIndex);
                break;
            case 1:
                d("DB render","1 "+rowIndex);
                //renderedView = renderString(format("%.1f",currRow.sugarLevel));
                renderedView = renderString(format("%.1f",currRow.sugarLevel/10f));
                break;
            case 2:
                d("DB render","2 "+rowIndex);
                renderedView = renderString(currRow.extra);
                break;
            default:
                d("DB render","default "+rowIndex);
                break;
        }
        return renderedView;
    }
      private View renderString(final String value) {
        final TextView textView = new TextView(getContext());
        textView.setText(value);
        textView.setPadding(20, 10, 20, 10);
        //textView.setTextSize(TEXT_SIZE);
        return textView;
    }
}
