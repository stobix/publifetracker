package stobix.app.lifetracker

import android.app.Dialog
import android.app.DialogFragment
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import stobix.utils.DateHandler
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by stobix on 2018-04-06.
 */

class GraphFragment:DialogFragment(){
    var gs:List<LineGraphSeries<DataPoint>> = mutableListOf()
    lateinit var entries:ArrayList<SugarEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("graph","got created");
        super.onCreate(savedInstanceState)
        entries = arguments
                .getParcelableArrayList<SugarEntry>("entries")
        val bareSeries = LineGraphSeries<DataPoint>(
                entries
                .map { DataPoint(
                        DateHandler(it.epochTimestamp).dateObject,
                        it.sugarLevel.toDouble()/10.0
                ) }
                .toTypedArray()
        )
        bareSeries.color = Color.GREEN
        val meanPerDaySeries =LineGraphSeries<DataPoint>(
                entries
                .groupBy { DateHandler(it.epochTimestamp).date }
                .map {
                    DataPoint(
                            DateHandler(it.value.first().epochTimestamp).dateObject,
                            it.value.sumBy { it.sugarLevel } .toDouble() / (it.value.size * 10.0)
                    ) }
                .toTypedArray()
        )
        meanPerDaySeries.isDrawBackground=true
        gs += bareSeries
        gs += meanPerDaySeries

        Log.d("graph","size: ${entries.size}")
    }

    fun SugarEntry.asDate() = DateHandler(this.epochTimestamp).dateObject
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d("graph","got viewed")
        //return super.onCreateView(inflater, container, savedInstanceState)
        val v = inflater!!.inflate(R.layout.graph_fragment,container,false)
        val graph = v.findViewById<GraphView>(R.id.graph)
        //graph.viewport.isYAxisBoundsManual = true
        graph.viewport.isXAxisBoundsManual = true
        graph.gridLabelRenderer.isHumanRounding = false
        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(activity)
        graph.gridLabelRenderer.numHorizontalLabels = 3
        graph.viewport.setMinX(entries.first().epochTimestamp.toDouble())
        graph.viewport.setMaxX(entries.last().epochTimestamp.toDouble())
        graph.viewport.isScalable=true
        gs.forEach { graph.addSeries(it) }
        return v
    }

}

