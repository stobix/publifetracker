package stobix.app.lifetracker

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import lecho.lib.hellocharts.formatter.SimpleColumnChartValueFormatter

import lecho.lib.hellocharts.gesture.ZoomType
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener
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

// Copied and modified from https://github.com/lecho/hellocharts-android/tree/master/hellocharts-samples/src/lecho/lib/hellocharts/samples/LineColumnDependencyActivity.java et al

typealias MeanValue = Float
typealias StartingTimestamp = Long
typealias Year = Int
typealias Month = Int
typealias Week = Int

typealias DateInfo = Pair<Year,Week>

typealias WeekPerMeanStructure = List<Pair<MeanValue, Map.Entry<DateInfo, List<Pair<SugarEntry, DateInfo>>>>>

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


        private lateinit var lineData: LineChartData
        private var columnData: ColumnChartData? = null
        lateinit var entries: List<SugarEntry>
        private lateinit var perWeekMean: WeekPerMeanStructure

        private lateinit var chartTop: LineChartView
        private lateinit var chartBottom: ColumnChartView

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



        private fun initiateBottomChart(
                perWeekMean: WeekPerMeanStructure) {

            val axisValues = ArrayList<AxisValue>()
            val columns = ArrayList<Column>()
            var values: MutableList<SubcolumnValue>

            for (i in perWeekMean.indices) {

                values = ArrayList()
                val (currentYear,currentWeek ) = perWeekMean[i].second.key
                val currentMean=perWeekMean[i].first

                values.add( SubcolumnValue(currentMean, colorBySugarLevel(currentMean)) )

                // The year should be displayed before the first week of the year
                if (currentWeek == 1)
                    axisValues.add(AxisValue(i.toFloat()).setLabel("$currentYear v$currentWeek"))
                else
                    axisValues.add(AxisValue(i.toFloat()).setLabel("v$currentWeek"))

                val c = Column(values)
                c.formatter = SimpleColumnChartValueFormatter(1)
                c.setHasLabelsOnlyForSelected(true)
                columns.add(c)
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

            chartBottom.currentViewport.right=perWeekMean.size.toFloat()-0.5f
            chartBottom.currentViewport.left=perWeekMean.size.toFloat()-15.5f

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
        }

        private fun updateTopChart(color: Int, columnIndex: Int, @Suppress("UNUSED_PARAMETER") value: SubcolumnValue) {
            // Cancel last animation if not finished.
            chartTop.cancelDataAnimation()
            val weekEntries = perWeekMean[columnIndex].second.value


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

            lineData.axisXBottom=Axis(weekEntries.mapIndexed {
                i, it ->
                val date = DateHandler(it.first.epochTimestamp)
                val hour = date.hour
                val minute = date.minute
                val day=days[date.weekDay]
                AxisValue(i.toFloat())
                        .setLabel("$day ${"%02d".format(hour)}:${"%02d".format(minute)}")}
            ).setHasLines(true).setHasTiltedLabels(true)

            val maxSugar = weekEntries.maxBy { it.first.sugarLevel } !!.first.sugarLevel/10f
            val altRight = if (weekEntries.size < 2 ) 2f else (weekEntries.size-1).toFloat()

            chartTop.currentViewport.right = altRight
            chartTop.currentViewport.top = maxSugar+1
            chartTop.maximumViewport.right = altRight
            chartTop.maximumViewport.top = maxSugar+1
            // Redraw the chart with the new line and viewport
            chartTop.invalidate()
        }

        private inner class BottomValueSelectedListener : ColumnChartOnValueSelectListener {

            override fun onValueSelected(columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue) {
                updateTopChart(value.color, columnIndex, value)
            }

            override fun onValueDeselected() {}

        }



        @Suppress("USELESS_CAST")
        private fun getMeanPerWeek(entries: List<SugarEntry>): WeekPerMeanStructure =
                // The "unnecessary" casts are to make the type be descriptive instead of a long jumble of ints
                entries
                        // get the year and week of each entry
                        .map {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis=it.epochTimestamp
                            it to (
                                    cal.get(Calendar.YEAR) as Year
                                    to
                                    cal.get(Calendar.WEEK_OF_YEAR) as Week
                            )
                        }
                        // group by year & week
                        .groupBy( { it.second } )
                        //
                        .map {
                            // mean value for the week
                            (( it.value.sumBy {it.first.sugarLevel} .toFloat() / (it.value.size * 10.0f) ) as MeanValue
                            to
                            it)
                        }


        companion object {
            // TODO internationalize these!
            val months = arrayOf("Jan", "Feb", "Mar", "Apr", "Maj", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec")

            val days = arrayOf("Mån", "Tis", "Ons", "Tor", "Fre", "Lör", "Sön")
        }
    }
}