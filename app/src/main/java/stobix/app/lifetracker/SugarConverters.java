package stobix.app.lifetracker;

import android.arch.persistence.room.TypeConverter;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

/**
 * Created by stobix on 2018-04-06.
 *
 * If I ever need converters to my SugarEntryDao, they will end up here.
 */

public class SugarConverters {

    @TypeConverter
    public LineGraphSeries<DataPoint> listToLineGraph(List<DataPoint> l) {
        return new LineGraphSeries<>(l.toArray(new DataPoint[l.size()]));
    }

    /* @TypeConverter
    public DataPoint getThaPoint(long timestamp, long sugar) {
        return new DataPoint(timestamp, sugar);
    }
    */
}
