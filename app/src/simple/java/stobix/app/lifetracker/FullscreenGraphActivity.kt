package stobix.app.lifetracker

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.support.v4.app.NavUtils
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.MenuItem
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.simple.activity_fullscreen_graph.*
import stobix.utils.ColorHandler
import stobix.utils.DateHandler
import java.text.SimpleDateFormat
import java.util.*
import stobix.utils.kotlinExtensions.to // this makes a to b to c create (a,b,c) instead of ((a,b),c)

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenGraphActivity : Activity() {
    private var gs:List<LineGraphSeries<DataPoint>> = mutableListOf()
    private lateinit var entries:List<SugarEntry>

    private lateinit var bareSeries: LineGraphSeries<DataPoint>
    private lateinit var meanPerDaySeries: LineGraphSeries<DataPoint>
    private lateinit var meanPerFourHourSeries: LineGraphSeries<DataPoint>
    private var showPerDay = 0

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // TODO GestureDetector to detect double click

        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreen_graph.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        actionBar?.show()
        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val c = ColorHandler(applicationContext)


        val themeSettings = applicationContext.theme.obtainStyledAttributes(
                R.styleable.FullscreenGraph)

        infix fun Int.orElse(c2: Int)=
                themeSettings.getColor(this,ContextCompat.getColor(applicationContext,c2))

        val bareSeriesColor =
                R.styleable.FullscreenGraph_allEntriesLineColor orElse
                        android.R.color.primary_text_dark

        val fourHourMeanSeriesColor =
                R.styleable.FullscreenGraph_fourHourMeanLineColor orElse
                        android.R.color.secondary_text_dark

        val chartBackgroundColor =
                R.styleable.FullscreenGraph_chartAreaColor orElse
                        android.R.color.background_dark

        val weekMeanSeriesColor =
                R.styleable.FullscreenGraph_weekMeanBackColor orElse
                        android.R.color.tertiary_text_dark

        setContentView(R.layout.activity_fullscreen_graph)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.
        // XXX
        // This is a horrid idea when using a graph!
        //  TODO Fix something better, like a drag-down thing
        fullscreen_graph.setOnLongClickListener {
            true
        }
        fullscreen_graph.setOnClickListener {
            toggleGraphShown()
        }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        dummy_button.setOnTouchListener(mDelayHideTouchListener)
        Log.d("graph","got created")
        super.onCreate(savedInstanceState)
        entries = intent.extras.getParcelableArrayList<SugarEntry>("entries")
                .filterNotNull()
                .sortedBy { it.epochTimestamp }

        bareSeries = LineGraphSeries(
                entries
                        .map { DataPoint(
                                DateHandler(it.epochTimestamp).dateObject,
                                it.sugarLevel.toDouble()/10.0
                        ) }
                        .toTypedArray()
        )

        val meanPerDayData =
                entries
                        // get the year and week of each entry
                        .map {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis=it.epochTimestamp
                            Pair(it, Pair(cal.get(Calendar.YEAR),cal.get(Calendar.WEEK_OF_YEAR)))
                        }
                        // sort the entries by year & week
                        .sortedWith(compareBy({it.second.first},{it.second.second}))
                        // group by year & week
                        .groupBy( { it.second } )
                        //
                        .map {
                            Triple(
                                    // For now, use the last entry in the week as timestamp.
                                    // For later, it's probably best to have a timestamp
                                    // corresponding to the start of the week or so
                                    it.value.sortedBy { it.first.epochTimestamp }.last().first.epochTimestamp
                                    ,
                                    // mean value for the week
                                    it.value.sumBy {it.first.sugarLevel} .toDouble() / (it.value.size * 10.0)
                                    // for debugging purposes only
                                    , it
                            ) }
                        // sort by timestamp. because somehow the data manages to get unsorted again (?)
                        .sortedBy { it.first }

        meanPerDaySeries = LineGraphSeries(
                meanPerDayData
                        // make DataPoints of the timestamp and week mean
                        .map {DataPoint(DateHandler(it.first).dateObject,it.second)}
                        // get a DataPoint array so LineGraphSeries gets happy
                        .toTypedArray()

        )

        val cal = Calendar.getInstance()
        cal.timeInMillis = 0
        cal.add(Calendar.HOUR,1)
        val hour = cal.timeInMillis

        val meanPerFourHourData =
                entries
                        .drop(1)
                        .fold(
                                entries.first().epochTimestamp
                                        to
                                        (entries.first().sugarLevel to 1)
                                        to
                                        listOf<Pair<Long,Double>>() )
                        {
                            acc , current ->
                            val (startTime: Long,meanAcc: Pair<Int, Int>,dataAcc ) = acc
                            val (accLevels: Int,points: Int) = meanAcc
                            if ( current.epochTimestamp - startTime >= 4*hour) {
                                val fourHourMean = accLevels.toDouble() / (points * 10)
                                (current.epochTimestamp
                                        to
                                        Pair(current.sugarLevel, 1)
                                        to
                                        dataAcc
                                                .plus(Pair(startTime, fourHourMean))
                                                .plus(Pair(startTime + 4 * hour, fourHourMean))
                                        )
                            }
                            else
                                ( startTime
                                        to
                                        Pair(accLevels+current.sugarLevel,points+1)
                                        to
                                        dataAcc
                                )
                        }
                        .third

        meanPerFourHourSeries = LineGraphSeries(
                meanPerFourHourData
                        .map {
                            DataPoint(DateHandler(it.first).dateObject, it.second)
                        }
                        .toTypedArray()
        )

        fun Calendar.between(first:Any, last: Any) = this.before(last) && this.after(first)

        bareSeries.color = bareSeriesColor
        bareSeries.thickness = 3
        meanPerFourHourSeries.color = fourHourMeanSeriesColor
//        meanPerFourHourSeries.isDrawDataPoints = true
        meanPerFourHourSeries.thickness = 3
        meanPerDaySeries.color=weekMeanSeriesColor
        meanPerDaySeries.isDrawBackground=true

        gs += bareSeries
        gs += meanPerDaySeries
        gs += meanPerFourHourSeries

        //graph.viewport.isYAxisBoundsManual = true
        fullscreen_graph.viewport.isXAxisBoundsManual = true
        fullscreen_graph.gridLabelRenderer.isHumanRounding = true
        fullscreen_graph.gridLabelRenderer.labelFormatter =
                DateAsXAxisLabelFormatter(this, SimpleDateFormat.getDateInstance())
        //fullscreen_graph.background=chartBackgroundColor
        fullscreen_graph.textAlignment = View.TEXT_ALIGNMENT_CENTER
        fullscreen_graph.gridLabelRenderer.numVerticalLabels = 4
        fullscreen_graph.viewport.setMinX((entries.last().epochTimestamp - 7*24*hour).toDouble())
        fullscreen_graph.viewport.setMaxX(entries.last().epochTimestamp.toDouble())
        fullscreen_graph.viewport.isScalable=true

        gs.forEach { fullscreen_graph.addSeries(it) }
        //fullscreen_graph.addSeries(meanPerDaySeries)
        //toggleGraphShown()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun toggleGraphShown(){
        when (showPerDay) {
            0 ->
                fullscreen_graph.removeSeries(meanPerFourHourSeries)
            1 -> {
                fullscreen_graph.removeSeries(bareSeries)
                fullscreen_graph.addSeries(meanPerFourHourSeries)
            }
            2 -> {
                fullscreen_graph.addSeries(bareSeries)
            }

        }
        showPerDay = (showPerDay+1) % 3
    }

    private fun hide() {
        // Hide UI first
        actionBar?.hide()
        fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        fullscreen_graph.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }
}
