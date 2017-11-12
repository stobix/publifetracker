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
        super(context, entries);
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        SugarEntry currRow = getRowData(rowIndex);
        View renderedView = null;
        switch(columnIndex) {
            case 0:
                String formatString = getResources().getString(R.string.dateFormat);
                DateFormat df = new DateFormat();
                Date myDate=new Date(currRow.epochTimestamp);
                String myDateString = df.format(formatString,myDate).toString();
                renderedView = renderString(myDateString);
                break;
            case 1:
                renderedView = renderString(format("%.1f",currRow.sugarLevel/10f));
                break;
            case 2:
                renderedView = renderString(currRow.extra);
                break;
            default:
                break;
        }
        return renderedView;
    }
      private View renderString(final String value) {
        final TextView textView = new TextView(getContext());
        textView.setText(value);
        textView.setPadding(20, 10, 20, 10);
        return textView;
    }
}
