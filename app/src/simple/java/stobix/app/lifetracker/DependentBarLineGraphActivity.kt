package stobix.app.lifetracker

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.TextView
import lecho.lib.hellocharts.formatter.SimpleColumnChartValueFormatter
import lecho.lib.hellocharts.gesture.ZoomType
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.util.ChartUtils
import lecho.lib.hellocharts.view.ColumnChartView
import lecho.lib.hellocharts.view.LineChartView
import stobix.utils.DateHandler
import stobix.utils.kotlinExtensions.Quadruple
import stobix.utils.kotlinExtensions.to
import stobix.utils.kotlinExtensions.to2
import java.lang.Exception
import java.util.*

// Copied and modified from https://github.com/lecho/hellocharts-android/tree/master/hellocharts-samples/src/lecho/lib/hellocharts/samples/LineColumnDependencyActivity.java et al

/**
 * The total whatever of a week. Sum, average, what have you
 */
// typealias WeekTotal = Float
typealias WeekTotal = Map<LowerCollationType, Float>

/**
 * A timestamp represented by seconds since epoch
 */
typealias Timestamp = Long

/**
 * An int representation of a year
 */
typealias Year = Int

/**
 *  An int representation of a moth
 */
typealias Month = Int

/**
 *  An int representation of a week
 */
typealias Week = Int

/**
 *  An int representation of a day
 */
typealias Day = Int

/**
 *
 */
typealias DateInfo = Pair<Year, Week>

//typealias AllWeeksList = List<Pair<WeekTotal, Pair<DateInfo, List<Pair<ValueEntry, DateInfo>>>>>
typealias WeekCollation = Pair<WeekTotal, Pair<DateInfo, Map<UpperCollationType, List<ValueEntry>>>>

typealias AllWeeksList = List<WeekCollation>

data class ValueEntry(var timestamp: Timestamp, var value: Float, var original: Int)

/**
 * Determines the type of a series, together with how to convert it
 */
enum class SeriesType(val divisor: Float) {
    DIRECT(1f) {
        override val convert = divideBy(this)
        override val unConvert = multiyBy(this)
    },
    FLOATYINT10(10f) {
        override val convert = divideBy(this)
        override val unConvert = multiyBy(this)
    },
    FLOATYINT100(100f) {
        override val convert = divideBy(this)
        override val unConvert = multiyBy(this)
    };

    abstract val convert: (value: Int)->Float
    abstract val unConvert: (value: Float)->Int

    protected fun divideBy(s: SeriesType): (Int)->Float = { it/s.divisor }

    protected fun multiyBy(s: SeriesType): (Float)->Int = { (it*s.divisor).toInt() }
}

enum class UpperCollationType {
    SUM_BY_DAY,
    AVG_BY_DAY,
    NONE
}

enum class LowerCollationType {
    SUM,
    AVG
}

/**
 * A data series to draw, together with various settings
 */
data class DataSeries(
        /**
         *
         */
        var description: String,
        /**
         *
         */
        var data: ArrayList<FloatyIntBucket>,
        /**
         * The resource id for the icon we click to show this series
         */
        var iconRes: Int,
        /**
         * How to convert the FloatyIntBucket values to the kind we want to show.
         */
        var valueType: SeriesType = SeriesType.FLOATYINT10,
        /**
         * A series of breakpoints for when we change color on the weekly summary
         */
        var breakPoints: DoubleArray = doubleArrayOf(4.0, 7.0, 15.0),
        /**
         * Whether to have zero at the bottom of the graph (like for insulin) or have the lowest actual value there (like for weight)
         */
        var keepLowZero: Boolean = true,
        /**
         * How we initially collate data on the "daily" upper view
         */
        var upperCollation: UpperCollationType = UpperCollationType.NONE,
        /**
         * How we initially collate data on the "daily" upper view
         */
        var lowerCollation: LowerCollationType = LowerCollationType.AVG

) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString()!!, // descr
            parcel.readBundle(DataSeries::class.java.classLoader)!!.getParcelableArrayList(
                    "data"
            )!!, // data
            parcel.readInt(), // iconRes
            SeriesType.valueOf(parcel.readString()!!), // valueType
            doubleArrayOf(), // breakpoints
            false,
            UpperCollationType.valueOf(parcel.readString()!!),
            LowerCollationType.valueOf(parcel.readString()!!)
    ) {
        var arrayLength = parcel.readInt()
        val dArray = DoubleArray(arrayLength)
        parcel.readDoubleArray(dArray)
        breakPoints = dArray
        arrayLength = parcel.readInt()
        val tempArray = BooleanArray(arrayLength)
        parcel.readBooleanArray(tempArray)
        keepLowZero = tempArray[0]
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        val b = Bundle()
        b.putParcelableArrayList("data", data)
        // Constructor args
        parcel.writeString(description)
        parcel.writeBundle(b)
        parcel.writeInt(iconRes)
        parcel.writeString(valueType.name)
        parcel.writeString(upperCollation.name)
        parcel.writeString(lowerCollation.name)

        // Aftermath
        parcel.writeInt(breakPoints.size)
        parcel.writeDoubleArray(breakPoints)
        val flagArray = booleanArrayOf(keepLowZero)
        parcel.writeInt(flagArray.size)
        parcel.writeBooleanArray(flagArray)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataSeries

        if (description != other.description) return false
        if (data != other.data) return false
        if (iconRes != other.iconRes) return false
        if (valueType != other.valueType) return false
        if (!breakPoints.contentEquals(other.breakPoints)) return false
        if (keepLowZero != other.keepLowZero) return false
        if (upperCollation != other.upperCollation) return false
        if (lowerCollation != other.lowerCollation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = description.hashCode()
        result = 31*result+data.hashCode()
        result = 31*result+iconRes
        result = 31*result+valueType.hashCode()
        result = 31*result+breakPoints.contentHashCode()
        result = 31*result+keepLowZero.hashCode()
        result = 31*result+upperCollation.hashCode()
        result = 31*result+lowerCollation.hashCode()
        return result
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


    // TODO Change every color according to current theme!

    lateinit var menuEntries: List<Pair<String, Int>>
    lateinit var fragment: PlaceholderFragment

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_weekly, menu)
        // Skapa en ikon för varje dataserie vi får in, och släng in i menyn.
        menuEntries.forEachIndexed { i, entry ->
            val (description, iconRes) = entry
            @Suppress("ReplaceSingleLineLet")
            menu?.let {
                it.add(0, i, 0, description)
                        // Default to a red X if the caller forgot to set iconRes
                        .setIcon(if (iconRes == 0) android.R.drawable.ic_delete else iconRes)
                        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId ?: 0
        Log.d("menu", "item selected: $id")
        fragment.menuItemClicked(id)
        return super.onOptionsItemSelected(item)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_column_dependency)
        if (savedInstanceState == null) {
            fragment = PlaceholderFragment()
            val args = intent.extras
            menuEntries = args.getParcelableArrayList<DataSeries>(
                    "series"
            ).map { it.description to it.iconRes }
            setTheme(args.getInt("theme"))
            fragment.arguments = args
            supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
        }
    }

    private interface MenuOptionReceiver {
        fun menuItemClicked(itemId: Int)
    }

    class PlaceholderFragment : Fragment(), MenuOptionReceiver {


        private lateinit var lineData: LineChartData
        private var columnData: ColumnChartData? = null
        private lateinit var rawSeries: ArrayList<DataSeries>
        private lateinit var series: List<Triple<AllWeeksList, (Float)->Int, DataSeries>>
        private lateinit var selectedSeries: Triple<AllWeeksList, (Float)->Int, DataSeries>
        private lateinit var perWeekMean: AllWeeksList

        private lateinit var chartTop: LineChartView
        private lateinit var chartBottom: ColumnChartView

        private var selectedWeek = 0
        private lateinit var topSeriesType: UpperCollationType
        private var topColor = 0


        /******************
         * Initiation
         ******************/
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val arguments = arguments ?: error("no args!")

            series = arguments.getParcelableArrayList<DataSeries>("series").map(::processDataSeries)

            val rootView = inflater.inflate(
                    R.layout.fragment_line_column_dependency, container, false
            )

            // *** TOP LINE CHART ***
            chartTop = rootView.findViewById<View>(R.id.chart_top) as LineChartView

            initiateTopChart()

            // *** BOTTOM COLUMN CHART ***

            chartBottom = rootView.findViewById<View>(R.id.chart_bottom) as ColumnChartView

            // set values for data set 0, and refresh bottom chart
            switchToDataSet(0)

            // fun tv(id:Int) =requireViewById<TextView>(id)
            fun tv(id: Int) = rootView.findViewById<TextView>(id) ?: throw Exception(
                    "$id not found lol"
            )

            var weekAvg = tv(R.id.BarLineGraphWeekAvg)
            var weekSum = tv(R.id.BarLineGraphWeekSum)
            var daySplit = tv(R.id.BarLineGraphDaySplit)
            var dayAvg = tv(R.id.BarLineGraphDayAvg)
            var daySum = tv(R.id.BarLineGraphDaySum)
            weekAvg.setOnClickListener { setBottomCollation(LowerCollationType.AVG) }
            weekSum.setOnClickListener { setBottomCollation(LowerCollationType.SUM) }
            daySplit.setOnClickListener { setTopCollation(UpperCollationType.NONE) }
            dayAvg.setOnClickListener { setTopCollation(UpperCollationType.AVG_BY_DAY) }
            daySum.setOnClickListener { setTopCollation(UpperCollationType.SUM_BY_DAY) }




            return rootView
        }

        /**
         * Initiate the top chart, and draw a 0 value for each day of the week.
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

            chartTop.setPadding(40, 0, 17, 43)

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


        /******************
         * Data processing
         ******************/


        private fun calculateColorByLevel(breakPoints: DoubleArray): (Float)->Int =
                when (breakPoints.size) {
                    0    -> {
                        { ChartUtils.COLOR_GREEN }
                    }
                    1    -> {
                        {
                            when {
                                it<breakPoints[0] -> ChartUtils.COLOR_GREEN
                                else              -> ChartUtils.COLOR_RED
                            }
                        }
                    }
                    2    -> {
                        {
                            when {
                                it<breakPoints[0] -> ChartUtils.COLOR_GREEN
                                it<breakPoints[1] -> ChartUtils.COLOR_ORANGE
                                else              -> ChartUtils.COLOR_RED
                            }
                        }
                    }
                    3    -> {
                        {
                            when {
                                it<breakPoints[0] -> ChartUtils.COLOR_VIOLET
                                it<breakPoints[1] -> ChartUtils.COLOR_GREEN
                                it<breakPoints[2] -> ChartUtils.COLOR_ORANGE
                                else              -> ChartUtils.COLOR_RED
                            }
                        }
                    }
                    // FIXME Any breakpoint above 4 is ignored for now. Maybe do this more dynamically for higher values?
                    else -> {
                        {
                            when {
                                it<breakPoints[0] -> ChartUtils.COLOR_VIOLET
                                it<breakPoints[1] -> ChartUtils.COLOR_GREEN
                                it<breakPoints[2] -> ChartUtils.COLOR_ORANGE
                                it<breakPoints[3] -> ChartUtils.COLOR_RED
                                else              -> ChartUtils.COLOR_BLUE
                            }
                        }
                    }
                }

        private fun processDataSeries(data: DataSeries): Triple<AllWeeksList, (Float)->Int, DataSeries> {
            val entries0 = data.data
                    // FIXME why do these make the graphs show correctly‽ I already sorted the data and made sure there were no nulls when I accessed the database
                    .filterNotNull()
                    .sortedBy { it.timestamp }

            val colorByLevel = calculateColorByLevel(data.breakPoints)

            val convertFromInt: (Int)->Float = data.valueType.convert
            val convertToInt: (Float)->Int = data.valueType.unConvert

            val mapper: (FloatyIntBucket)->ValueEntry = {
                ValueEntry(it.timestamp, convertFromInt(it.value), it.value)
            }

            val entries = entries0.map(mapper)
            // The "unnecessary" casts are to make the type be descriptive instead of a long jumble of ints
            @Suppress("USELESS_CAST")
            // get the year and week of each entry
            val yearweekEntries = entries.map {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.timestamp
                val year: Year = cal.get(Calendar.YEAR)
                val week: Week = cal.get(Calendar.WEEK_OF_YEAR)
                val day: Day = cal.get(Calendar.DAY_OF_WEEK)
                val dateInfo: DateInfo = year to week
                it to dateInfo to day
            }
            // group by year & week
            val yearWeekGrouping =
                    yearweekEntries.groupBy { it.second }

            val perDayGroupings =
                    yearWeekGrouping.map { calculateGroupElement(it.key, it.value, convertToInt) }


            // calculate week total data for each grouping
            val collationPerWeek = perDayGroupings.map {
                val elems = it.second[data.upperCollation]!!
                @Suppress("NestedLambdaShadowedImplicitParameter")
                val weekSum = elems.sumBy { it.original }
                val weekConverted = convertFromInt(weekSum)
                val weekTotal: WeekTotal = LowerCollationType.values().fold(
                        mutableMapOf()
                ) { acc, type ->
                    val tot = when (type) {
                        LowerCollationType.AVG -> weekConverted/elems.size
                        LowerCollationType.SUM -> weekConverted
                    }
                    acc[type] = tot
                    acc
                }
                val retVal: WeekCollation = (weekTotal to it)
                retVal
            }
            return collationPerWeek to colorByLevel to data
        }

        private fun groupByDay(elems: List<Triple<ValueEntry, DateInfo, Day>>) =
                elems
                        .sortedBy { (first) -> first.timestamp }
                        .groupBy { elem -> elem.third }

        private fun calculateDailySumEntry(entries: List<Triple<ValueEntry, DateInfo, Day>>, convertToInt: (Float)->Int) =
                entries.sumByDouble { (first) -> first.value.toDouble() }
                        .toFloat()
                        .let { sum ->
                            ValueEntry(
                                    entries.first().first.timestamp,
                                    sum,
                                    convertToInt(sum)
                            )
                        }

        private fun calculateDailyAvgEntry(entries: List<Triple<ValueEntry, DateInfo, Day>>, convertToInt: (Float)->Int) =
                entries.sumByDouble { (first) -> first.value.toDouble() }
                        .div(entries.size.toDouble())
                        .toFloat()
                        .let { avg ->
                            ValueEntry(
                                    entries.first().first.timestamp,
                                    avg,
                                    convertToInt(avg)
                            )
                        }

        private fun calculateCollationsForEntries(entries: List<Triple<ValueEntry, DateInfo, Day>>, convertToInt: (Float)->Int) =
                mapOf(
                        UpperCollationType.NONE to entries.map { it.first },
                        UpperCollationType.SUM_BY_DAY to {
                            groupByDay(entries).map {
                                calculateDailySumEntry(
                                        it.value, convertToInt
                                )
                            }
                        }(),
                        UpperCollationType.AVG_BY_DAY to {
                            groupByDay(entries).map {
                                calculateDailyAvgEntry(
                                        it.value, convertToInt
                                )
                            }
                        }()
                )

        private fun calculateGroupElement(date: DateInfo, entries: List<Triple<ValueEntry, DateInfo, Day>>, convertToInt: (Float)->Int) =
                date to2 calculateCollationsForEntries(entries, convertToInt)

        /******************
         * Menu Click Handling / Data Switching
         ******************/
        override fun menuItemClicked(itemId: Int) {
            Log.d("menu", "item click received $itemId")
            if (itemId<series.size) {
                Log.d("menu", "switching data sets")
                switchToDataSet(itemId)
            }
        }

        private fun switchToDataSet(index: Int) {
            selectedSeries = series[index]
            topSeriesType = selectedSeries.third.upperCollation
            activity?.run { title = selectedSeries.third.description }

            perWeekMean = selectedSeries.first
            initiateTopChart()
            initiateBottomChart()
        }

        private fun initiateBottomChart() {
            setBottomCollation(selectedSeries.third.lowerCollation)
        }

        private fun setBottomCollation(lowerCollation: LowerCollationType) {
            refreshBottomChart(
                    selectedSeries.second, selectedSeries.third.keepLowZero, lowerCollation
            )
        }

        private fun refreshBottomChart(colorByLevel: (Float)->Int, keepLowZero: Boolean, selectedSeries: LowerCollationType) {

            val axisValues = ArrayList<AxisValue>()
            val columns = ArrayList<Column>()
            var values: MutableList<SubcolumnValue>

            for (i in perWeekMean.indices) {

                values = ArrayList()
                val (currentYear, currentWeek) = perWeekMean[i].second.first
                val currentMean = perWeekMean[i].first[selectedSeries] ?: 0f

                values.add(SubcolumnValue(currentMean, colorByLevel(currentMean)))

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
            chartBottom.setPadding(40, 0, 0, 0)

            // Set selection mode to keep selected month column highlighted.
            chartBottom.isValueSelectionEnabled = true

            chartBottom.zoomType = ZoomType.HORIZONTAL

            chartBottom.currentViewport.right = perWeekMean.size.toFloat()-0.5f
            chartBottom.currentViewport.left = perWeekMean.size.toFloat()-15.5f

            if (!keepLowZero) {
                val minVal = perWeekMean
                        .map { it.first[selectedSeries] ?: 0f }.min()?.minus(2f) ?: 0f
                chartBottom.currentViewport.bottom = minVal
            }

            // TODO switch to the same week for the new data set that we had for the old iff we had an old data set and it had data the same week!
            // TODO When switching back and forth between data sets, put back the previously selected week when switching to a previously selected data set.
        }

        /******************
         * Column click handling
         ******************/

        private inner class BottomValueSelectedListener : ColumnChartOnValueSelectListener {

            override fun onValueSelected(columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue) {
                updateTopChart(
                        value.color, topSeriesType ?: selectedSeries.third.upperCollation,
                        columnIndex
                )
            }

            override fun onValueDeselected() {}
        }

        private fun setTopCollation(collationType: UpperCollationType) {
            topSeriesType = collationType
            updateTopChart()
        }

        private fun updateTopChart(color: Int, collationType: UpperCollationType, columnIndex: Int) {
            selectedWeek = columnIndex
            topColor = color
            topSeriesType = collationType
            updateTopChart()
        }

        private fun updateTopChart() {
            val columnIndex = selectedWeek
            val color = topColor
            val collationType = topSeriesType
            // Cancel last animation if not finished.
            chartTop.cancelDataAnimation()
            // FIXME this is the only place outside refreshBottomChart that references perWeekMean. Can I change this to make perWeekMean not be class global?
            val weekEntries = perWeekMean[columnIndex].second.second[collationType]!!

            // Create data points for the current week
            val newLineVals = weekEntries.mapIndexed { i, it ->
                val sugarValue = it.value
                val date = DateHandler(it.timestamp).date
                PointValue(i.toFloat(), sugarValue).setLabel("$date\n $sugarValue")
            }

            val line = lineData.lines[0]// For this example there is always only one line.
            line.values = newLineVals
            line.color = color
            line.setHasLabelsOnlyForSelected(true)
            val lines = ArrayList<Line>()
            lines += line
            lineData.lines = lines

            lineData.axisXBottom = Axis(weekEntries.mapIndexed { i, (timestamp) ->
                val date = DateHandler(timestamp)
                val day = days[date.weekDay]
                AxisValue(i.toFloat())
                        .setLabel("$day ${"%02d".format(date.hour)}:${"%02d".format(date.minute)}")
            }
            ).setHasLines(true).setHasTiltedLabels(true)

            val maxValue = weekEntries.maxBy { it.original }!!.value
            val minValue = weekEntries.minBy { it.original }!!.value
            val altRight = if (weekEntries.size<2) 2f else (weekEntries.size-1).toFloat()

            chartTop.currentViewport.right = altRight
            chartTop.currentViewport.top = maxValue+1
            chartTop.currentViewport.bottom = minValue-1 // TODO include a button to toggle this between min and 0!
            chartTop.maximumViewport.right = altRight
            chartTop.maximumViewport.top = maxValue+1
            // Redraw the chart with the new line and viewport
            chartTop.invalidate()
        }

        companion object {
            // TODO internationalize these!
            val months = arrayOf(
                    "Jan", "Feb", "Mar", "Apr", "Maj", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov",
                    "Dec"
            )
            val days = arrayOf("Mån", "Tis", "Ons", "Tor", "Fre", "Lör", "Sön")
        }
    }

    companion object {
        @JvmStatic
        fun createBarLineActivityBundle(title: String, data: ArrayList<DataSeries>) =
                appendBarLineActivityBundle(title, data, Bundle())

        @JvmStatic
        fun appendBarLineActivityBundle(title: String, data: ArrayList<DataSeries>, bundle: Bundle): Bundle {
            // TODO check if there are previous values in the bundle to consider
            bundle.putString("title", title)
            bundle.putParcelableArrayList("series", data)
            return bundle
        }
    }
}