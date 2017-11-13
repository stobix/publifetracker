package com.example.stobix.myapplication

import android.content.Context
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.util.ArrayList
import java.util.Date

import de.codecrafters.tableview.TableDataAdapter

import android.util.Log.d
import java.lang.String as JString

/**
 * Created by stobix on 11/11/17.
 */

class SugarEntryTableDataAdapter(context: Context, entries: ArrayList<SugarEntry>) : TableDataAdapter<SugarEntry>(context, entries) {

    override fun getCellView(rowIndex: Int, columnIndex: Int, parentView: ViewGroup): View {
        val currRow = getRowData(rowIndex)
        return when (columnIndex) {
            0 -> {
                val formatString = resources.getString(R.string.dateFormat)
                val myDate = Date(currRow.epochTimestamp)
                val myDateString = DateFormat.format(formatString, myDate).toString()
                renderString(myDateString)
            }
            1 ->  renderString(JString.format("%.1f", currRow.sugarLevel / 10f))
            2 ->  renderString(currRow.extra)
            else -> renderString("")
            }
    }

    private fun renderString(value: String): View {
        val textView = TextView(context)
        textView.text = value
        textView.setPadding(20, 10, 20, 10)
        return textView
    }

}