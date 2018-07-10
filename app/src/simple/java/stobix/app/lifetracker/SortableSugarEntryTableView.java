package stobix.app.lifetracker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

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

        themeSettings.recycle();

        Resources res = getResources();

        String[] hdrs = {res.getStringArray(R.array.headers)[0],""};
        final SimpleTableHeaderAdapter simpleTableHeaderAdapter =
                new SimpleTableHeaderAdapter(context,hdrs);

        simpleTableHeaderAdapter.setTextColor(headerColor);
        setHeaderAdapter(simpleTableHeaderAdapter);


        setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(rowColorEven, rowColorOdd));
        setHeaderSortStateViewProvider(SortStateViewProviders.brightArrows());

        final TableColumnWeightModel tableColumnWeightModel = new TableColumnWeightModel(hdrs.length);

        tableColumnWeightModel.setColumnWeight(0, 2);
        tableColumnWeightModel.setColumnWeight(1, 5);
        setColumnModel(tableColumnWeightModel);

        setColumnComparator(0, (a,b) -> {
            long x= a.getEpochTimestamp() - b.getEpochTimestamp();
            return (x>0)?1:(x<0)?-1:0; // since we can't be sure the difference isn't bigger than an int
        });
        setColumnComparator(1, (a,b) -> {
                    if (a.compareSugar(b) != 0) return a.compareSugar(b);
                    else if (a.compareWeight(b) != 0) return a.compareWeight(b);
                    else if (a.compareTreatment(b) != 0) return a.compareTreatment(b);
                    else if (a.compareFood(b) != 0) return a.compareFood(b);
                    else return a.compareExtra(b);
                }
        );
    }

}
