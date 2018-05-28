package stobix.app.lifetracker

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import lecho.lib.hellocharts.gesture.ZoomType
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener
import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.AxisValue
import lecho.lib.hellocharts.model.Column
import lecho.lib.hellocharts.model.ColumnChartData
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PointValue
import lecho.lib.hellocharts.model.SubcolumnValue
import lecho.lib.hellocharts.model.Viewport
import lecho.lib.hellocharts.util.ChartUtils
import lecho.lib.hellocharts.view.ColumnChartView
import lecho.lib.hellocharts.view.LineChartView
import stobix.utils.DateHandler
import java.util.*
import stobix.utils.pair_extensions.to // this makes a to b to c create an (a,b,c) instead of ((a,b),c)

// Copied and modified from https://github.com/lecho/hellocharts-android/tree/master/hellocharts-samples/src/lecho/lib/hellocharts/samples/LineColumnDependencyActivity.java et al

typealias MeanValue = Float
typealias StartingTimestamp = Long
typealias Year = Int
typealias Month = Int
typealias Week = Int

class DependentBarLineGraphActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_column_dependency)
        if (savedInstanceState == null) {
            val fragment = PlaceholderFragment()
            val args = intent.extras
            fragment.arguments = args
            supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {


        lateinit var lineData: LineChartData
        private var columnData: ColumnChartData? = null
        lateinit var entries: List<SugarEntry>
        lateinit var perWeekMean: List<Triple<StartingTimestamp, MeanValue, Map.Entry<Triple<Year, Week, Month>, List<Pair<SugarEntry, Triple<Year, Week, Month>>>>>>

        lateinit var chartTop: LineChartView
        lateinit var chartBottom: ColumnChartView

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            entries = arguments!!.getParcelableArrayList<SugarEntry>("entries")
                    .filterNotNull()
                    .sortedBy { it.epochTimestamp }
            val rootView = inflater.inflate(R.layout.fragment_line_column_dependency, container, false)

            // *** TOP LINE CHART ***
            chartTop = rootView.findViewById<View>(R.id.chart_top) as LineChartView

            perWeekMean = getMeanPerWeek(entries)

            // Generate and set data for line chart
            initiateTopChart()

            // *** BOTTOM COLUMN CHART ***

            chartBottom = rootView.findViewById<View>(R.id.chart_bottom) as ColumnChartView

            initiateBottomChart(perWeekMean)

            return rootView
        }

        private fun colorBySugarLevel(level: Float) = when{
            level > 15 -> ChartUtils.COLOR_RED
            level > 7 -> ChartUtils.COLOR_ORANGE
            level < 4 -> ChartUtils.COLOR_VIOLET
            else -> ChartUtils.COLOR_GREEN
        }

        private fun initiateBottomChart(perWeekMean: List<Triple<StartingTimestamp, MeanValue, Map.Entry<Triple<Year, Week, Month>, List<Pair<SugarEntry, Triple<Year, Week, Month>>>>>>) {

            val numSubcolumns = 4
            val numColumns = months.size

            val axisValues = ArrayList<AxisValue>()
            val columns = ArrayList<Column>()
            var values: MutableList<SubcolumnValue>

            for (i in perWeekMean.indices) {

                values = ArrayList()
                val (currentYear,currentWeek ,currentMonth) = perWeekMean[i].third.key
                val currentMean=perWeekMean[i].second
//                for (j in 0 until perWeekMean[i].third.value.size) {
//                    values.add(SubcolumnValue(Math.random().toFloat() * 50f + 5, ChartUtils.pickColor()))
//                }

                values.add(SubcolumnValue(currentMean, colorBySugarLevel(currentMean)))

                //axisValues.add(AxisValue(currentWeek.toFloat()).setLabel(months[currentMonth]))
                if (currentWeek == 1)
                    axisValues.add(AxisValue(i.toFloat()).setLabel("$currentYear v$currentWeek"))
                else
                    axisValues.add(AxisValue(i.toFloat()).setLabel("v$currentWeek"))

                columns.add(Column(values).setHasLabelsOnlyForSelected(true))
            }

            columnData = ColumnChartData(columns)

            columnData!!.axisXBottom = Axis(axisValues).setHasLines(false)
            columnData!!.axisYLeft = Axis().setHasLines(true).setMaxLabelChars(2)

            chartBottom.columnChartData = columnData

            // Set value touch listener that will trigger changes for chartTop.
            chartBottom.onValueTouchListener = BottomValueSelectedListener()

            // Set selection mode to keep selected month column highlighted.
            chartBottom.isValueSelectionEnabled = true

            chartBottom.zoomType = ZoomType.HORIZONTAL

            // chartBottom.setOnClickListener(new View.OnClickListener() {
            //
            // @Override
            // public void onClick(View v) {
            // SelectedValue sv = chartBottom.getSelectedValue();
            // if (!sv.isSet()) {
            // initiateTopChart();
            // }
            //
            // }
            // });

        }

        /**
         * Generates initial data for line chart. At the beginning all Y values are equals 0. That will change when user
         * will select value on column chart.
         */
        private fun initiateTopChart() {
            val numValues = 7

            val axisValues = ArrayList<AxisValue>()
            val values = ArrayList<PointValue>()
            for (i in 0 until numValues) {
                values.add(PointValue(i.toFloat(), 0f))
                axisValues.add(AxisValue(i.toFloat()).setLabel(days[i%7]))
            }

            val line = Line(values)
            line.setColor(ChartUtils.COLOR_GREEN).isCubic = true
            line.setHasLabelsOnlyForSelected(true)

            val lines = ArrayList<Line>()
            lines.add(line)

            lineData = LineChartData(lines)
            lineData.axisXBottom = Axis(axisValues).setHasLines(true)
            lineData.axisYLeft = Axis().setHasLines(true).setMaxLabelChars(3)

            chartTop.setPadding(0,0,17,40)

            chartTop.lineChartData = lineData

            // For build-up animation you have to disable viewport recalculation.
            chartTop.isViewportCalculationEnabled = false

            // And set initial max viewport and current viewport- remember to set viewports after data.
            val v = Viewport(0f, 25f, 6f, 0f)
            chartTop.maximumViewport = v
            chartTop.currentViewport = v

            chartTop.isValueSelectionEnabled = true
            chartTop.zoomType = ZoomType.HORIZONTAL
            chartTop.onValueTouchListener = TopValueSelectedListener()
        }


        private fun clearLineData(color: Int) {
            // Cancel last animation if not finished.
            chartTop.cancelDataAnimation()

            // Modify data targets
            val line = lineData.lines[0]// For this example there is always only one line.
            line.color = color
            for (value in line.values) {
                // Change target only for Y value.
                value.setTarget(value.x, 0f)
            }

            // Start new data animation with 300ms duration;
            chartTop.startDataAnimation(300)
        }

        private fun updateTopChart(color: Int, columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue) {
            Log.d("linedata","$columnIndex $value")
            // Cancel last animation if not finished.
            chartTop.cancelDataAnimation()
            val weekEntries = perWeekMean[columnIndex].third.value

            Log.d("linedata","$weekEntries")


            // Create data points for the current week
            val newLineVals=weekEntries.mapIndexed { i,it ->
                val sugarValue =it.first.sugarLevel.toFloat()/10f
                PointValue(i.toFloat(),sugarValue).setLabel(sugarValue.toString())
            }

            val line = lineData.lines[0]// For this example there is always only one line.
            line.values = newLineVals
            line.color = color
            line.setHasLabelsOnlyForSelected(true)
            val lines=ArrayList<Line>()
            lines+=line
            lineData.lines=lines


            val timestamps = weekEntries.map{it.first.epochTimestamp }

            val cal = Calendar.getInstance()


            lineData.axisXBottom=Axis(weekEntries.mapIndexed {
                i, it ->
                val date = DateHandler(it.first.epochTimestamp)
                val hour = date.hour
                val minute = date.minute
                Log.d("linedata","${it.first.epochTimestamp} is a ${days[date.weekDay]}")
                val day=days[date.weekDay]
                AxisValue(i.toFloat()).setLabel("$day $hour:$minute")}
            ).setHasLines(true).setHasTiltedLabels(true)

            val maxSugar = weekEntries.maxBy { it.first.sugarLevel } !!.first.sugarLevel/10f
            val newViewport = if (weekEntries.size < 2)
                Viewport(
                        0f,
                        maxSugar+1,
                        1f,
                        0f)
            else
                Viewport(
                        0f,
                        maxSugar+1,
                        weekEntries.size.toFloat()-1,
                        0f)
            chartTop.currentViewport.right = weekEntries.size.toFloat()-1
            chartTop.currentViewport.top = maxSugar+1
            chartTop.maximumViewport.right = weekEntries.size.toFloat()-1
            chartTop.maximumViewport.top = maxSugar+1
            chartTop.invalidate()
            //chartTop.refreshDrawableState()
            // Makes the chart update how many values to use, I think
        }
        private inner class TopValueSelectedListener : LineChartOnValueSelectListener {
            override fun onValueSelected(lineIndex: Int, pointIndex: Int, value: PointValue?) {
                Log.d("linedata","Point selected: $lineIndex, $pointIndex, $value")
            }


            override fun onValueDeselected() {
                Log.d("linedata","Point deselected")
            }
        }

        private inner class BottomValueSelectedListener : ColumnChartOnValueSelectListener {

            override fun onValueSelected(columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue) {

                updateTopChart(value.color, columnIndex, subcolumnIndex, value)
            }

            override fun onValueDeselected() {

                //clearLineData(ChartUtils.COLOR_GREEN)

            }
        }



        private fun getMeanPerWeek(entries: List<SugarEntry>) =
                entries
                        // get the year and week of each entry
                        .map {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis=it.epochTimestamp
                            it to (
                                    cal.get(Calendar.YEAR) as Year
                                    to
                                    cal.get(Calendar.WEEK_OF_YEAR) as Week

                                    to
                                    cal.get(Calendar.MONTH) as Month
                            )
                        }
                        // sort the entries by year & week
                        // FIXME Shouldn't be necessary, right?
                        .sortedWith(compareBy({it.second.first},{it.second.second}))
                        // group by year & week
                        .groupBy( { it.second } )
                        //
                        .map {
                            Triple(
                                    // FIXME Do I need to sort this again?
                                    // corresponding to the start of the week or so
                                    ( it.value.sortedBy { it.first.epochTimestamp }.first().first.epochTimestamp ) as StartingTimestamp
                                    ,
                                    // mean value for the week
                                    ( it.value.sumBy {it.first.sugarLevel} .toFloat() / (it.value.size * 10.0f) ) as MeanValue
                                    // for
                                    , it
                            ) }
                        // FIXME fix this
                        // sort by timestamp. because somehow the data manages to get unsorted again (?)
                        .sortedBy { it.first }


        companion object {
            // TODO internationalize these!
            val months = arrayOf("Jan", "Feb", "Mar", "Apr", "Maj", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec")

            val days = arrayOf("Mån", "Tis", "Ons", "Tor", "Fre", "Lör", "Sön")
        }
    }
}