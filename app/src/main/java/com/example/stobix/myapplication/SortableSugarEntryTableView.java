package com.example.stobix.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import java.util.ArrayList;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;

/**
 * Created by stobix on 11/11/17.
 *
 */

public class SortableSugarEntryTableView extends SortableTableView<SugarEntry> {
    public SortableSugarEntryTableView(final Context context) {
        this(context,null);
    }
    public SortableSugarEntryTableView(final Context context, final AttributeSet attributes){
        this(context, attributes, android.R.attr.listViewStyle);
    }


    public SortableSugarEntryTableView(final Context context, final AttributeSet attributes, final int styleAttributes) {
        super(context,attributes,styleAttributes);

        TypedArray themeSettings = context.getTheme().obtainStyledAttributes(attributes,
                R.styleable.SortableSugarEntryTableView, 0, 0);

        final int rowColorEven = themeSettings.getColor(
                R.styleable.SortableSugarEntryTableView_table_data_row_even,
                ContextCompat.getColor(context, R.color.table_data_row_even)
        );

        final int rowColorOdd = themeSettings.getColor(
                R.styleable.SortableSugarEntryTableView_table_data_row_odd,
                ContextCompat.getColor(context, R.color.table_data_row_odd)
        );

        final int headerColor = themeSettings.getColor(
                R.styleable.SortableSugarEntryTableView_table_header_text,
                ContextCompat.getColor(context, R.color.colorPrimary)
        );

        // TODO How to get the default text color, and how to set header color??
        final int textColor = themeSettings.getColor(
                R.styleable.SortableSugarEntryTableView_table_data_text,
                ContextCompat.getColor(context, R.color.colorAccent)
        );


        Resources res = getResources();

        String[] hdrs = res.getStringArray(R.array.headers);
        final SimpleTableHeaderAdapter simpleTableHeaderAdapter =
                new SimpleTableHeaderAdapter(context,hdrs);

        simpleTableHeaderAdapter.setTextColor(headerColor);
        setHeaderAdapter(simpleTableHeaderAdapter);


        setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(rowColorEven, rowColorOdd));
        setHeaderSortStateViewProvider(SortStateViewProviders.brightArrows());

        final TableColumnWeightModel tableColumnWeightModel =
                // TODO Maybe change this to something not dependant on the amount of headers in the current language, but the actual number of fields to be shown.
                new TableColumnWeightModel(hdrs.length);

        tableColumnWeightModel.setColumnWeight(0, 2);
        tableColumnWeightModel.setColumnWeight(1, 1);
        tableColumnWeightModel.setColumnWeight(2, 3);
        setColumnModel(tableColumnWeightModel);

        setColumnComparator(0, (a,b) -> {
            long x= a.getEpochTimestamp() - b.getEpochTimestamp();
            return (x>0)?1:(x<0)?-1:0;
        });
        setColumnComparator(1, (a,b) -> a.getSugarLevel() - b.getSugarLevel());
        setColumnComparator(2, (a,b) -> {
                    if(a.getExtra()== null) return 1;
                    else if(b.getExtra() == null) return -1;
                    else return a.getExtra().compareTo(b.getExtra());
                }
        );
    }

}
