@file:Suppress("UNUSED_PARAMETER")

package stobix.app.lifetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import stobix.app.lifetracker.MainActivity.COLOR_THEMES
import stobix.utils.ColorHandler
import stobix.utils.DateHandler
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

/**
 * Created by stobix on 2/2/18.
 */

class BuildVariantSpecificCode {
    companion object {
        /**
         * Build variant specific onLoad code.
         */
        @JvmStatic
        fun onLoad(c: Context) {
        }

        /**
         * Menu selection handling
         */
        @JvmStatic
        fun handleMenu(c: MainActivity, item: Int) =
                when (item) {
                    /*
                case R.id.action_settings:
                    Log.i("MenuClick", "onOptionsItemSelected: ");
                    return true;

                case R.id.action_colors:

                    ColorPickerDialogBuilder
                            .with(this)
                            .setTitle("Choose color")
                            .initialColor(0xFFFFFFFF)
                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                            .density(12)
                            .setOnColorSelectedListener(selectedColor ->
                                    d("COLOR", "Colour selected:"
                                            + Integer.toHexString(selectedColor)))
                            .setPositiveButton("ok", (dialog, selectedColor, allColors) ->
                                // TODO Use the color for something.
                                    d("COLOR", "Colour confirmed:"
                                            + Integer.toHexString(selectedColor)))
                            .setNegativeButton("cancel", (dialog, which) ->
                                    d("COLOR", "Colour aborted"))
                            .build()
                            .show();

                    return true;
                    */


                    R.id.show_stats               -> {
                        val h = MainActivity.MainHandler(c) { main, b ->
                            val message = b.getString("message")
                            val builder = AlertDialog.Builder(main)
                            builder.setTitle(R.string.stat_window_title)
                                    .setMessage(message)
                                    .setPositiveButton(
                                            android.R.string.yes
                                    ) { dialog, which -> }
                                    .setIcon(android.R.drawable.ic_dialog_info)
                                    .show()
                        }

                        thread(start = true) {
                            val m = h.obtainMessage()
                            val b = Bundle()
                            var message = ""

                            // Get all sugar levels mean
                            var levels = c.dao.allSugarLevels
                            Log.d("show_stats", "levels: ${levels.size}")
                            var s = 0L
                            for (l in levels) s += l
                            message += c.getString(R.string.stat_window_header_total_bs)+"\n"
                            if (s == 0L)
                                message += c.getString(R.string.stat_window_no_bs)+"\n"
                            else {
                                val totAvg = s.toDouble()/(levels.size*10)
                                message += String.format(
                                        Locale.getDefault(),
                                        "\t"+c.getString(R.string.stat_window_avg_bs)+"\n", totAvg
                                )
                                message += "\t"+c.getString(
                                        R.string.stat_window_bs_entries
                                )+levels.size+"\n"
                            }

                            // Get previous 30 days sugar levels mean
                            val dateHandler = DateHandler()
                            levels = c.dao.getAllSugarLevels(
                                    dateHandler.clone().subtractDays(30).timestamp,
                                    dateHandler.timestamp
                            )
                            message += c.getString(R.string.stat_window_header_total_bs_30)+"\n"
                            s = 0L
                            for (l in levels) s += l
                            if (s == 0L)
                                message += c.getString(R.string.stat_window_no_bs)+"\n"
                            else {
                                val totAvg = s.toDouble()/(levels.size*10)
                                message += String.format(
                                        Locale.getDefault(), "\t"+
                                        c.getString(R.string.stat_window_avg_bs)+"\n", totAvg
                                )
                                message += "\t"+c.getString(
                                        R.string.stat_window_bs_entries
                                )+levels.size+"\n"
                            }

                            b.putString("message", message)
                            m.data = b
                            h.sendMessage(m)
                        }
                        true to true
                    }

                    R.id.show_graphs              -> {
                        val graphHandler = MainActivity.MainHandler(c) { mainActivity, bundle ->
                            Log.d("graph", "got bundle")
                            val i = Intent(c, FullscreenGraphActivity::class.java)
                            i.putExtras(bundle)
                            c.startActivity(i)
                        }

                        thread(start = true) {
                            Log.d("graph", "got request")
                            val m = graphHandler.obtainMessage()
                            val b = Bundle()
                            val entries = c.dao.allSugarBuckets
                            Log.d("graph", " request")
                            val entryArrayList = ArrayList<FloatyIntBucket>(entries)
                            b.putParcelableArrayList("entries", entryArrayList)
                            val ch = ColorHandler(c)
                            val colorsIDs = java.util.ArrayList<Int>()
                            colorsIDs.add(android.R.attr.textColorPrimary)
                            colorsIDs.add(android.R.attr.textColorSecondary)
                            colorsIDs.add(android.R.attr.textColorTertiary)
                            colorsIDs.add(R.attr.colorPrimary)
                            colorsIDs.add(R.attr.table_data_row_odd)
                            colorsIDs.add(R.attr.table_data_row_even)
                            colorsIDs.sort()
                            ch.withColorMap(
                                    colorsIDs
                            ) { colorMap ->
                                val colorValues = java.util.ArrayList<Int>()
                                // These are in an array instead of sending individiual
                                // values simply because this works and the other
                                // method didn't
                                colorValues.add(0, colorMap[android.R.attr.textColorPrimary] ?: 0)
                                colorValues.add(1, colorMap[android.R.attr.textColorSecondary] ?: 0)
                                colorValues.add(2, colorMap[android.R.attr.textColorSecondary] ?: 0)
                                colorValues.add(3, colorMap[R.attr.colorPrimary] ?: 0)
                                colorValues.add(4, colorMap[R.attr.table_data_row_even] ?: 0)
                                colorValues.add(5, colorMap[R.attr.table_data_row_odd] ?: 0)
                                b.putIntegerArrayList("colors", colorValues)
                            }

                            m.data = b
                            Log.d("graph", "sending bundle")
                            graphHandler.sendMessage(m)
                        }
                        true to true
                    }
                    R.id.action_colors            -> {
                        val colorEditorHandler = MainActivity.MainHandler(
                                c
                        ) { mainActivity, bundle ->
                            Log.d("graph", "got bundle")
                            val i = Intent(c, ColorEditorActivity::class.java)
                            i.putExtras(bundle)
                            c.startActivity(i)
                        }
                        thread(start = true) {
                            val m = colorEditorHandler.obtainMessage()
                            colorEditorHandler.sendMessage(m)
                        }
                        true to true
                    }


                    R.id.show_sugar_weekly        -> {
                        // En MainHandler tar ett Message från en tråd, packar upp dess Bundle och
                        // skickar vidare till en funktion tillsammans med en referens
                        // till this för MainActivity.
                        // Detta behövs eftersom en Handler måste vara static, så funktionen nedan kan
                        // inte direkt hänvisa till MainActivity som this utan referensen måste skickas
                        // in som ett argument till funktionen. (Annars kunde jag skrivit this istället
                        // för mainActivity nedan.)
                        val newGraphHandler = MainActivity.MainHandler(
                                c
                                // Körs när all data hämtats från databasen och lagts i en bundle
                        ) { mainActivity, bundle ->
                            val i = Intent(mainActivity, DependentBarLineGraphActivity::class.java)
                            // släng in bundlen i intent så activityn kan hämta ut data och färgtema
                            i.putExtras(bundle)
                            // Starta grafactivity
                            c.startActivity(i)
                        }

                        // Databashantering måste ske i en separat tråd.
                        thread(start = true) {
                            val m = newGraphHandler.obtainMessage()
                            // Alla blodsockervärden
                            val allSugarBuckets = c.dao.allSugarBuckets
                            // Alla viktvärden
                            val allWeightBuckets = c.dao.allWeightBuckets
                            val sleepStr = c.getString(R.string.EntryCreatorSleepString)
                            val allSleepBuckets = c.dao.getAllIntervalsLike("$sleepStr:%")
                            val allJobBuckets = c.dao.getAllIntervalsLike("Jobb")
                            Log.d(
                                    "veckografer",
                                    "sockervärden: ${allSugarBuckets.size} viktvärden: ${allWeightBuckets.size}"
                            )
                            // Lista med dataserier
                            val a = java.util.ArrayList<DataSeries>()
                            // Allt som behövs för att skapa blodsockergrafen
                            a.add(
                                    DataSeries(
                                            // Menynamn om ikonen ej får plats.
                                            c.getString(R.string.input_sugar),
                                            // Data
                                            ArrayList(allSugarBuckets), // Ikonresursen
                                            R.drawable.blood_sugar_icon,
                                            // Typ av data ("floatyInt10" = float med en decimal som sparats som int i databasen.
                                            // Exempelvis 10.2f sparas som 102)
                                            SeriesType.FLOATYINT10,
                                            // Skiljevärden mellan olika färger för grafen.
                                            // Här: lila under 4, grönt innan 7, brandgult innan 15, rött över 15
                                            doubleArrayOf(4.0, 7.0, 15.0),
                                            // Skall veckomedelsstaplarna börja från 0 eller från lägsta uppmätta medel?
                                            true
                                    )
                            )
                            // Samma fast för viktgrafen
                            a.add(
                                    DataSeries(
                                            c.getString(R.string.input_weight),
                                            ArrayList(allWeightBuckets),
                                            R.drawable.weight_icon,
                                            SeriesType.FLOATYINT10,
                                            doubleArrayOf(80.0, 85.0, 90.0),
                                            false
                                    )
                            )
                            a.add(
                                    DataSeries(
                                            c.getString(R.string.EntryCreatorSleepString),
                                            ArrayList(
                                                    allSleepBuckets.map { it.toFloatyIntBucket() }),
                                            R.drawable.zzz_icon,
                                            SeriesType.FLOATYINT10,
                                            doubleArrayOf(6.0, 9.0, 12.0),
                                            true,
                                            UpperCollationType.SUM_BY_DAY,
                                            LowerCollationType.AVG
                                    )
                            )
                            a.add(
                                    DataSeries(
                                            "Jobb",
                                            ArrayList(allJobBuckets.map { it.toFloatyIntBucket() }),
                                            R.drawable.datetime_icon,
                                            SeriesType.FLOATYINT10,
                                            doubleArrayOf(2.0, 5.0, 6.0),
                                            true,
                                            UpperCollationType.SUM_BY_DAY,
                                            LowerCollationType.SUM
                                    )
                            )
                            // Skapar en bundle med dataserierna inlagda.
                            val b = DependentBarLineGraphActivity
                                    .createBarLineActivityBundle("Veckografer", a)
                            // Skicka med appens tema
                            b.putInt("theme", c.currentTheme)
                            m.data = b
                            // Starta MainHandlern, så funktionen ovan innan tråden körs.
                            newGraphHandler.sendMessage(m)
                        }
                        true to true
                    }

                    R.id.action_switch_theme      -> {
                        ThemePickerDialog(c, COLOR_THEMES).show()
                        true to true
                    }

                    R.id.action_import_db         -> {
                        c.fa.userReplaceDb()
                        true to true
                    }

                    R.id.action_merge_db          -> {
                        c.fa.userMergeDb()
                        true to true
                    }

                    R.id.action_export_db         -> {
                        c.fa.userCreateFile()
                        true to true
                    }

                    R.id.action_toggle_list_icons -> {
                        c.doChangeIconVisibility()
                        true to true
                    }

                    else                          -> false to true //
                }

    }
}
