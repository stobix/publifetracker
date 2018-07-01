package stobix.app.lifetracker

import android.content.Context
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import de.codecrafters.tableview.TableDataAdapter
import java.util.*

/**
 * Created by stobix on 11/11/17.
 * This class tells the table how to display a SugarEntry
 */

class SugarEntryTableDataAdapter(
        context: Context,
        entries: ArrayList<SugarEntry>
) : TableDataAdapter<SugarEntry>(context, entries) {

    override fun getCellView(rowIndex: Int, columnIndex: Int, parentView: ViewGroup): View {
        val currRow = getRowData(rowIndex)
        return when (columnIndex) {
            0 -> {
                val formatString = resources.getString(R.string.dateTimeFormat)
                val myDate = Date(currRow.epochTimestamp)
                val myDateString = DateFormat.format(formatString, myDate).toString()
                renderString(myDateString)
            }

            1 -> renderStrings(
                        currRow.sugarVal()?.let { "$it mmol/l"}
                        ,
                        currRow.weightVal()?.let { "$it kg" }
                        ,
                        currRow.extra
                )
            else -> renderString(null)
        }
    }

    private fun SugarEntry.sugarVal()=
        if(this.sugarLevel > 0)
            // This apparently uses either a comma or a dot in the output depending on the language
            String.format("%.1f", this.sugarLevel / 10f)
        else
            null

    private fun SugarEntry.weightVal() =
            this.weight?.let { String.format("%.1f", it / 10f) }

    private fun renderStrings(vararg values: String?): View{
        val compositeView = LinearLayout(context)
        values.filterNotNull().forEach { compositeView.addView(renderString(it)) }
        return compositeView
    }
    private fun renderString(value: String?): View {
        val textView = TextView(context)
        textView.text = value ?: ""
        textView.setPadding(20, 10, 20, 10)
        return textView
    }
}
