package com.example.stobix.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;

/**
 * Created by stobix on 9/4/17.
 */

public class SortableLolFnissTableView extends SortableTableView<LoL> {
    public SortableLolFnissTableView(final Context context) {
        this(context, null);
    }

    public SortableLolFnissTableView(final Context context, final AttributeSet attributes) {
        this(context, attributes, android.R.attr.listViewStyle);
    }

    private String getString(int id){
        return getResources().getString(id);
    }

    public SortableLolFnissTableView(final Context context, final AttributeSet attributes, final int styleAttributes) {
        super(context, attributes, styleAttributes);

        Resources res = getResources();

        String[] hdrs = res.getStringArray(R.array.headers);
        final SimpleTableHeaderAdapter simpleTableHeaderAdapter =
                new SimpleTableHeaderAdapter(context,hdrs);


        simpleTableHeaderAdapter.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        setHeaderAdapter(simpleTableHeaderAdapter);

        final int rowColorEven = ContextCompat.getColor(context, R.color.table_data_row_even);
        final int rowColorOdd = ContextCompat.getColor(context, R.color.table_data_row_odd);
        setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(rowColorEven, rowColorOdd));
        setHeaderSortStateViewProvider(SortStateViewProviders.brightArrows());

        final TableColumnWeightModel tableColumnWeightModel =
                // TODO Maybe change this to something not dependant on the amount of headers in the current language, but the actual number of fields to be shown.
                new TableColumnWeightModel(hdrs.length);

        tableColumnWeightModel.setColumnWeight(0, 2);
        tableColumnWeightModel.setColumnWeight(1, 3);
        tableColumnWeightModel.setColumnWeight(2, 3);
        setColumnModel(tableColumnWeightModel);

        setColumnComparator(0, (a,b) -> a.getDate().compareTo(b.getDate()));
        setColumnComparator(1, (a,b) -> a.getSugar()-b.getSugar());
        setColumnComparator(2, (a,b) -> a.getExtra().compareTo(b.getExtra()));

    }

}
