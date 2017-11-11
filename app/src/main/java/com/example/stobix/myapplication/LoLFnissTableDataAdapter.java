package com.example.stobix.myapplication;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.codecrafters.tableview.TableDataAdapter;

import static java.lang.String.format;

/**
 * Created by stobix on 9/1/17.
 */

class LoLFnissTableDataAdapter extends TableDataAdapter<LoL> {

    public LoLFnissTableDataAdapter(Context context, LoL[] data) {
        super(context, data);
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        LoL fniss = getRowData(rowIndex);
        View renderedView = null;

        switch (columnIndex) {
            case 0:
                //renderedView = renderDate(fniss);
                String formatString = getResources().getString(R.string.dateFormat);
                android.text.format.DateFormat df = new android.text.format.DateFormat();
                String myDate = df.format(formatString,fniss.getDate()).toString();
                renderedView = renderString(myDate);
                break;
            case 1:
                renderedView = renderString(format("%.1f",fniss.getSugarF()));
                //renderedView = renderBlood(fniss);
                break;
            case 2:
                renderedView = renderString(fniss.getExtra());
                //renderedView = renderExtra(fniss);
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
