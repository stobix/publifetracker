package stobix.app.lifetracker

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import lecho.lib.hellocharts.formatter.SimpleColumnChartValueFormatter
import lecho.lib.hellocharts.gesture.ZoomType
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.util.ChartUtils
import lecho.lib.hellocharts.view.ColumnChartView
import lecho.lib.hellocharts.view.LineChartView
import stobix.utils.DateHandler
import stobix.utils.kotlinExtensions.to
import java.util.*

// Copied and modified from https://github.com/lecho/hellocharts-android/tree/master/hellocharts-samples/src/lecho/lib/hellocharts/samples/LineColumnDependencyActivity.java et al

typealias MeanValue = Float
typealias StartingTimestamp = Long
typealias Timestamp = Long
typealias Value = Int
typealias Year = Int
typealias Month = Int
typealias Week = Int

typealias DateInfo = Pair<Year,Week>

typealias WeekPerMeanStructure = List<Pair<MeanValue, Map.Entry<DateInfo, List<Pair<ValueEntry, DateInfo>>>>>


data class ValueEntry (var timestamp: Timestamp, var value: Float, var original: Int)

data class DataSeries (
        var description: String,
        var data: ArrayList<FloatyIntBucket>,
        var iconRes:Int,
        var valueType:String="floatyInt10",
        var breakPoints: DoubleArray = doubleArrayOf(4.0,7.0,15.0),
        var keepLowZero: Boolean = true

): Parcelable
{
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readBundle(DataSeries::class.java.classLoader).getParcelableArrayList("data"),
            parcel.readInt(),
            parcel.readString(),
            doubleArrayOf(),
            false

    ){
        var arrayLength = parcel.readInt()
        val dArray = DoubleArray(arrayLength)
        parcel.readDoubleArray(dArray)
        breakPoints=dArray
        arrayLength = parcel.readInt()
        val tempArray= BooleanArray(arrayLength)
        parcel.readBooleanArray(tempArray)
        keepLowZero = tempArray[0]
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        val b=Bundle()
        b.putParcelableArrayList("data",data)
        parcel.writeString(description)
        parcel.writeBundle(b)
        parcel.writeInt(iconRes)
        parcel.writeString(valueType)
        parcel.writeInt(breakPoints.size)
        parcel.writeDoubleArray(breakPoints)
        val flagArray =booleanArrayOf(keepLowZero)
        parcel.writeInt(flagArray.size)
        parcel.writeBooleanArray(flagArray)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataSeries> {
        override fun createFromParcel(parcel: Parcel): DataSeries {
            return DataSeries(parcel)
        }

        override fun newArray(size: Int): Array<DataSeries?> {
            return arrayOfNulls(size)
        }
    }

}


class DependentBarLineGraphActivity : AppCompatActivity() {


    lateinit var menuEntries:List<String>
    lateinit var fragment: PlaceholderFragment

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_weekly,menu)
        menuEntries.forEachIndexed { i, s ->
            menu?.add(0,i,0,s)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId ?: 0
        Log.d("menu","item selected: $id")
        fragment.menuItemClicked(id)
        return super.onOptionsItemSelected(item)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_column_dependency)
        if (savedInstanceState == null) {
            fragment = PlaceholderFragment()
            val args = intent.extras
            menuEntries = args.getParcelableArrayList<DataSeries>("series").map{it.description}
            fragment.arguments = args
            supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
        }
    }

    private interface MenuOptionReceiver{
        fun menuItemClicked(itemId: Int)
    }

    /**
     * A placeholder fragment containing a simple view
     */
    class PlaceholderFragment : Fragment(), MenuOptionReceiver{
        override fun menuItemClicked(itemId: Int) {
            Log.d("menu","item click received $itemId")
            if(itemId < series.size ) {
                Log.d("menu","switching data sets")
                switchToDataSet(itemId)
            }
        }

        private lateinit var lineData: LineChartData
        private var columnData: ColumnChartData? = null
        private lateinit var rawSeries: ArrayList<DataSeries>
        private lateinit var series: List<Triple<WeekPerMeanStructure,(Float)->Int,Boolean>>
        private lateinit var perWeekMean: WeekPerMeanStructure

        private lateinit var chartTop: LineChartView
        private lateinit var chartBottom: ColumnChartView

        private var keepLowZero = true

        private fun processDataSeries(data:DataSeries): Triple<WeekPerMeanStructure, (level: Float) -> Int, Boolean> {
            val entries0 = data.data
                    // FIXME why do these make the graphs show correctly‽ I already sorted the data and made sure there were no nulls when I accessed the database
                    .filterNotNull()
                    .sortedBy { it.timestamp }

            val thingToPick = data.valueType
            val breakPoints = data.breakPoints
            val colorByLevel:(Float) -> Int = when ( breakPoints.size ) {
                0 -> {
                    {
                        ChartUtils.COLOR_GREEN
                    }
                }
                1 -> {
                    {
                        when {
                            it < breakPoints[0] -> ChartUtils.COLOR_GREEN
                            else -> ChartUtils.COLOR_RED
                        }
                    }

                }
                2 -> {
                    {
                        when{
                            it < breakPoints[0] -> ChartUtils.COLOR_GREEN
                            it < breakPoints[1] -> ChartUtils.COLOR_ORANGE
                            else -> ChartUtils.COLOR_RED
                        }
                    }

                }
                3 -> {
                    {
                        when{
                            it < breakPoints[0] -> ChartUtils.COLOR_VIOLET
                            it < breakPoints[1] -> ChartUtils.COLOR_GREEN
                            it < breakPoints[2] -> ChartUtils.COLOR_ORANGE
                            else -> ChartUtils.COLOR_RED
                        }
                    }
                }
                // FIXME Any breakpoint above 4 is ignored for now. Maybe do this more dynamically?
                else ->
                {
                    {
                        when{
                            it < breakPoints[0] -> ChartUtils.COLOR_VIOLET
                            it < breakPoints[1] -> ChartUtils.COLOR_GREEN
                            it < breakPoints[2] -> ChartUtils.COLOR_ORANGE
                            it < breakPoints[3] -> ChartUtils.COLOR_RED
                            else -> ChartUtils.COLOR_BLUE
                        }
                    }
                }
            }

            val convertFromInt :(Int) -> Float = when(thingToPick){
                "int" -> {{it.toFloat()}}
                "floatyInt10" -> {{it.toFloat()/10f}}
                "floatyInt100" -> {{it.toFloat()/100f}}
                else -> {{it.toFloat()}}
            }

            val mapper : (FloatyIntBucket) -> ValueEntry = {
                ValueEntry(it.timestamp,convertFromInt(it.value),it.value)
            }

            val entries = entries0.map(mapper)
            val weekPerMean = getMeanPerWeek(entries,convertFromInt)
            return weekPerMean to colorByLevel to data.keepLowZero
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val arguments = arguments ?: error("no args!")

            rawSeries = arguments.getParcelableArrayList<DataSeries>("series")
            series = rawSeries.map { processDataSeries(it) }

            val rootView = inflater.inflate(R.layout.fragment_line_column_dependency, container, false)

            // *** TOP LINE CHART ***
            chartTop = rootView.findViewById<View>(R.id.chart_top) as LineChartView

            // Initiate the top chart, and draw some default values.
            initiateTopChart()

            // *** BOTTOM COLUMN CHART ***

            chartBottom = rootView.findViewById<View>(R.id.chart_bottom) as ColumnChartView

            // set values for data set 0, and refresh bottom chart
            switchToDataSet(0)

            return rootView
        }


        private fun switchToDataSet(index: Int){
            perWeekMean = series[index].first
            val colorByLevel = series[index].second
            keepLowZero = series[index].third
            refreshBottomChart(colorByLevel)
        }


        private fun refreshBottomChart(colorByLevel: (Float) -> Int) {

            val axisValues = ArrayList<AxisValue>()
            val columns = ArrayList<Column>()
            var values: MutableList<SubcolumnValue>

            for (i in perWeekMean.indices) {

                values = ArrayList()
                val (currentYear,currentWeek ) = perWeekMean[i].second.key
                val currentMean=perWeekMean[i].first

                values.add( SubcolumnValue(currentMean, colorByLevel(currentMean)) )

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
            // Todo: set left to the width of the max characters shown on the axis
            chartBottom.setPadding(40,0,0,0)

            // Set selection mode to keep selected month column highlighted.
            chartBottom.isValueSelectionEnabled = true

            chartBottom.zoomType = ZoomType.HORIZONTAL

            chartBottom.currentViewport.right=perWeekMean.size.toFloat()-0.5f
            chartBottom.currentViewport.left=perWeekMean.size.toFloat()-15.5f

            if (!keepLowZero)
                chartBottom.currentViewport.bottom= perWeekMean.minBy { it.first } ?.first?.minus(2f) ?: 0f



            // TODO switch to the same week for the new data set that we had for the old iff we had an old data set and it had data the same week!
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

            chartTop.setPadding(40,0,17,43)

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

        private var topColor: Int = 0
        private var selectedColumn: Int? = null

        private fun updateTopChart(color: Int, columnIndex: Int){
            // Save these so we can refresh without knowing the index
            topColor = color
            selectedColumn = columnIndex
            // Cancel last animation if not finished.
            chartTop.cancelDataAnimation()
            val weekEntries = perWeekMean[columnIndex].second.value


            // Create data points for the current week
            val newLineVals=weekEntries.mapIndexed { i,it ->
                val sugarValue =it.first.value
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
                val date = DateHandler(it.first.timestamp)
                val hour = date.hour
                val minute = date.minute
                val day=days[date.weekDay]
                AxisValue(i.toFloat())
                        .setLabel("$day ${"%02d".format(hour)}:${"%02d".format(minute)}")}
            ).setHasLines(true).setHasTiltedLabels(true)

            val maxValue = weekEntries.maxBy { it.first.original } !!.first.value
            val minValue = weekEntries.minBy { it.first.original } !!.first.value
            val altRight = if (weekEntries.size < 2 ) 2f else (weekEntries.size-1).toFloat()

            chartTop.currentViewport.right = altRight
            chartTop.currentViewport.top = maxValue+1
            chartTop.currentViewport.bottom = minValue-1 // TODO include a button to toggle this between min and 0!
            chartTop.maximumViewport.right = altRight
            chartTop.maximumViewport.top = maxValue+1
            // Redraw the chart with the new line and viewport
            chartTop.invalidate()
        }

        private inner class BottomValueSelectedListener : ColumnChartOnValueSelectListener {

            override fun onValueSelected(columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue) {
                updateTopChart(value.color, columnIndex)
            }

            override fun onValueDeselected() {}

        }



        @Suppress("USELESS_CAST")
        private fun getMeanPerWeek(entries: List<ValueEntry>,convertFromInt:(Int) -> Float): WeekPerMeanStructure =
                // The "unnecessary" casts are to make the type be descriptive instead of a long jumble of ints
                entries
                        // get the year and week of each entry
                        .map {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis=it.timestamp
                            val year: Year = cal.get(Calendar.YEAR)
                            val week: Week = cal.get(Calendar.WEEK_OF_YEAR)
                            val dateInfo: DateInfo = year to week
                            it to dateInfo
                        }
                        // group by year & week
                        .groupBy { it.second }
                        //
                        .map {
                            val elems = it.value
                            val weekSum = elems.sumBy {it.first.original}
                            val weekConverted = convertFromInt(weekSum)
                            val weekMean: MeanValue = weekConverted / elems.size
                            (weekMean to it)
                        }


        companion object {
            // TODO internationalize these!
            val months = arrayOf("Jan", "Feb", "Mar", "Apr", "Maj", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec")

            val days = arrayOf("Mån", "Tis", "Ons", "Tor", "Fre", "Lör", "Sön")

        }
    }

    companion object {
        @JvmStatic
        fun createBarLineActivityBundle(title: String, data: ArrayList<DataSeries>)=
            appendBarLineActivityBundle(title,data,Bundle())
        @JvmStatic
        fun appendBarLineActivityBundle(title: String, data: ArrayList<DataSeries>,bundle:Bundle): Bundle {
            // TODO check if there are previous values in the bundle to consider
            bundle.putString("title",title)
            bundle.putParcelableArrayList("series", data)
            return bundle
        }
    }
}