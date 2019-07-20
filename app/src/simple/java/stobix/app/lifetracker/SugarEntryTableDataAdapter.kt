package stobix.app.lifetracker

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

    private infix fun<A> A.pairedIfDefined(b: String?) = b?.let { this to it }

    override fun getCellView(rowIndex: Int, columnIndex: Int, parentView: ViewGroup): View {
        val currRow = getRowData(rowIndex)
        return when (columnIndex) {
            0 -> {
                val formatString = resources.getString(R.string.dateTimeFormat)
                val myDate = Date(currRow.timestamp)
                val myDateString = DateFormat.format(formatString, myDate).toString()
                renderString(myDateString)
            }

            1 ->
                if((context as MainActivity).displaysListIcons())
                    renderStringsWithIcons(
                            R.drawable.blood_sugar_icon pairedIfDefined currRow.sugarLevel?.let { String.format("%.1f mmol/l", it / 10f)}
                            ,
                            R.drawable.weight_icon pairedIfDefined currRow.weight?.let { String.format("%.1f kg", it / 10f) }
                            ,
                            R.drawable.food_icon pairedIfDefined currRow.food
                            ,
                            R.drawable.drink_icon pairedIfDefined currRow.drink
                            ,
                            R.drawable.treatment_icon pairedIfDefined currRow.insulin ?.let { String.format("%.1f insulin", it )}
                            ,
                            R.drawable.pills_icon pairedIfDefined currRow.treatment
                            ,
                            R.drawable.extra_icon pairedIfDefined currRow.extra
                    )
                else
                    renderStrings(parentView,
                            currRow.sugarLevel?.let { String.format("%.1f mmol/l", it / 10f)}
                            ,
                            currRow.weight?.let { String.format("%.1f kg", it / 10f) }
                            ,
                            currRow.food
                            ,
                            currRow.drink
                            ,
                            currRow.insulin ?.let { String.format("%.1f insulin", it )}
                            ,
                            currRow.treatment
                            ,
                            currRow.extra
                    )
            else -> renderString("N/A")
        }
    }

    private fun renderStringsWithIcons(vararg values: Pair<Int,String>?): View{
        val compositeView = LinearLayout(context)
        compositeView.orientation = LinearLayout.VERTICAL
        val metrics = (context as MainActivity).screenMetrics
        values.filterNotNull().forEach {
            if (it.second!="" ) {
                val textView = renderString(it.second)
                // Needed to get the text height
                textView.measure(metrics.widthPixels,0)
                val imageSideLen =  textView.measuredHeight.times(1.3).toInt()

                val iconView = ImageView(context)
                iconView.setImageResource(it.first)
                iconView.setPadding(20, 10, 20, 10)

                // Set the size of the image view
                val iconParams = LinearLayout.LayoutParams(imageSideLen,imageSideLen)
                iconView.layoutParams = iconParams
                iconView.maxHeight = imageSideLen

                val entryPointView = LinearLayout(context)
                entryPointView.addView(iconView)
                entryPointView.addView(textView)

                compositeView.addView(entryPointView)
            }
        }
        return compositeView
    }

    private fun renderStrings(parentView: ViewGroup,vararg values: String?): View{
        val compositeView = LinearLayout(context)
        compositeView.orientation = LinearLayout.VERTICAL
        var horizView = LinearLayout(context)

        parentView.measure(0,0)
        val prevWidth = parentView.measuredWidth
        val maxWidth = (context as MainActivity).screenMetrics.widthPixels - prevWidth
        var sumWidth = 0
        values.filterNotNull().forEach {
            if (it!="" ) {
                val textView = renderString(it)
                textView.measure(0,0)
                if( textView.measuredWidth+sumWidth < maxWidth ){
                    Log.d("widths","${textView.measuredWidth}+$sumWidth < $maxWidth")
                    horizView.addView( textView )
                    sumWidth+=textView.measuredWidth
                } else {
                    Log.d("widths","${textView.measuredWidth}+$sumWidth > $maxWidth")
                    compositeView.addView(horizView)
                    horizView = LinearLayout(context)
                    horizView.addView(textView)
                    sumWidth = textView.measuredWidth
                }
            }
        }
        compositeView.addView(horizView)
        return compositeView
    }

    private fun renderString(value: String): View {
        val textView = TextView(context)
        textView.text = value
        textView.setPadding(20, 10, 20, 10)
        return textView
    }
}
