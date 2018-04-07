package stobix.app.lifetracker

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.support.v4.app.NavUtils
import android.util.Log
import android.view.MenuItem
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.simple.activity_fullscreen_graph.*
import stobix.utils.DateHandler
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenGraphActivity : Activity() {
    private var gs:List<LineGraphSeries<DataPoint>> = mutableListOf()
    private lateinit var entries:ArrayList<SugarEntry>

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
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

        setContentView(R.layout.activity_fullscreen_graph)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.
        //fullscreen_graph.setOnClickListener { toggle() }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        dummy_button.setOnTouchListener(mDelayHideTouchListener)
        Log.d("graph","got created")
        super.onCreate(savedInstanceState)
        entries = intent.extras.getParcelableArrayList<SugarEntry>("entries")
        val bareSeries = LineGraphSeries<DataPoint>(
                entries
                        .map { DataPoint(
                                DateHandler(it.epochTimestamp).dateObject,
                                it.sugarLevel.toDouble()/10.0
                        ) }
                        .toTypedArray()
        )
        bareSeries.color = Color.GREEN

        val e1 = entries
                .map {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis=it.epochTimestamp
                    Pair(it, Pair(cal.get(Calendar.YEAR),cal.get(Calendar.WEEK_OF_YEAR)))
                }
                .sortedWith(compareBy({it.second.first},{it.second.second}))
        val e2 = e1 .groupBy( { it.second } )
        val e3 = e2 .map {
            Triple(
                    it.value.sortedBy { it.first.epochTimestamp }.first().first.epochTimestamp
                    ,
                    it.value.sumBy {it.first.sugarLevel} .toDouble() / (it.value.size * 10.0)
                    , it
            ) }
        val e4 = e3 .sortedBy { it.first }
        val meanPerDayPoints = e4

        meanPerDayPoints.forEach {
            Log.d("points","$it")
        }
        val meanPerDaySeries = LineGraphSeries<DataPoint>(
                meanPerDayPoints.map {DataPoint(DateHandler(it.first).dateObject,it.second)}.toTypedArray()
        )
        /*
        val meanPerDaySeries =LineGraphSeries<DataPoint>(
                entries
                        .groupBy { DateHandler(it.epochTimestamp).date }
                        .toSortedMap(compareBy({it.first},{it.second},{it.third}))
                        .map {
                            DataPoint(
                                    DateHandler(it.value.first().epochTimestamp)
                                            // place in the middle of the day
                                            .setTime(12,0)
                                            .dateObject
                                    ,
                                    it
                                            .value
                                            .sumBy { it.sugarLevel }
                                            .toDouble() / (it.value.size * 10.0)
                            ) }
                        .toTypedArray()
        )
        */
        meanPerDaySeries.isDrawBackground=true
        meanPerDaySeries.isDrawAsPath = true
        gs += bareSeries
        gs += meanPerDaySeries

        //graph.viewport.isYAxisBoundsManual = true
        fullscreen_graph.viewport.isXAxisBoundsManual = true
        fullscreen_graph.gridLabelRenderer.isHumanRounding = true
        fullscreen_graph.gridLabelRenderer.labelFormatter =
                DateAsXAxisLabelFormatter(this, SimpleDateFormat.getDateInstance())
        fullscreen_graph.textAlignment = View.TEXT_ALIGNMENT_CENTER
        fullscreen_graph.gridLabelRenderer.numVerticalLabels = 4
        fullscreen_graph.viewport.setMinX(entries.first().epochTimestamp.toDouble())
        fullscreen_graph.viewport.setMaxX(entries.last().epochTimestamp.toDouble())
        fullscreen_graph.viewport.isScalable=true

        gs.forEach { fullscreen_graph.addSeries(it) }
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
