package stobix.app.lifetracker

import android.content.Context
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.codecrafters.tableview.TableDataAdapter
import stobix.view.containerview.Container
import stobix.view.containerview.ContainerView
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
            1 -> {
                if(currRow.sugarLevel > 0)
                    // TODO set locale somewhere in the app so this displays a decimal comma in countries that use it
                    renderString(String.format("%.1f", currRow.sugarLevel / 10f))
                else
                    renderString("")
            }

            2 -> {
                // renderString("${with(currRow.weight," kg, ","")} ${currRow.extra ?: ""}")
                val cv = ContainerView(context)
                val c = Container(rowIndex)
                var g = c
                for (i in 0..rowIndex) {
                    var f = Container()
                    g.addContainer(f)
                    g=f
                }
                g.addInt(rowIndex,"row")
                // cv.textSize = 30f
                cv.container = c
                cv.container.addString("cookies",currRow.epochTimestamp.toInt())
                cv.showContents = true
                cv.showIntDescriptions = true
                cv.recurLevel = 7
                cv
            }
            else -> renderString("N/A")
        }
    }

    private fun with(a: Any?, ifIs: String, ifNull: String) =
            if (a != null)
                "$a $ifIs"
            else
                ifNull

    private fun renderString(value: String): View {
        val textView = TextView(context)
        textView.text = value
        textView.setPadding(20, 10, 20, 10)
        return textView
    }
}
