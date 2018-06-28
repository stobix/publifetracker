package stobix.app.lifetracker

import android.app.Dialog
import android.content.Context
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import stobix.utils.ColorHandler

class ThemePickerDialog
    (private val ctx: Context, private val themes: ArrayList<ThemeListItem>) : Dialog(ctx)
{
    override fun show() {
        super.show()
        this.setContentView(R.layout.theme_picker)
        this.setTitle(context.getString(R.string.theme_picker_dialog_title))
        val themesList = this.findViewById<ListView>(R.id.themePickerList)

        val adapter  = ThemeArrayAdapter(ctx, themes)
        themesList.adapter = adapter
        themesList.onItemClickListener = AdapterView.OnItemClickListener {
            _: AdapterView<*>, _: View, _viewedRow: Int, index: Long ->
            Log.d("click","$index, which is ${themes[index.toInt()]}")
            (ctx as MainActivity).doSetTheme(themes[index.toInt()].themeResourceValue)
            dismiss()
        }

    }
}

class ThemeArrayAdapter(ctx: Context,  items: ArrayList<ThemeListItem>)
    :
        ArrayAdapter<ThemeListItem>(ctx,R.layout.theme_picker_item_view,items)
{
    val layout = R.layout.theme_picker_item_view
    val c = ColorHandler(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val theme = getItem(position)

        val view = convertView ?: LayoutInflater.from(context)!!.inflate(layout,parent,false)

        val color1 = view.findViewById<ImageView>(R.id.themeViewColor1)
        val color2 = view.findViewById<ImageView>(R.id.themeViewColor2)
        val color3 = view.findViewById<ImageView>(R.id.themeViewColor3)
        val textView = view.findViewById<TextView>(R.id.themeViewText)
        val slantedBarFill = view.findViewById<ImageView>(R.id.themeViewSlantedBarFill)
        val slantedBar1 = view.findViewById<ImageView>(R.id.themeViewSlantedBar1)
        val slantedBar2 = view.findViewById<ImageView>(R.id.themeViewSlantedBar2)
        val slantedBar3 = view.findViewById<ImageView>(R.id.themeViewSlantedBar3)

        c.themeRes = theme.themeResourceValue
        val colorList =
                listOf(R.attr.colorPrimary,
                        R.attr.colorAccent,
                        android.R.attr.textColorPrimary,
                        R.attr.table_data_row_even,
                        R.attr.tableView_headerColor,
                        android.R.attr.textColorTertiary,
                        R.attr.table_header_text,
                        R.attr.table_data_row_odd,
                        R.attr.button_plus_color
                )
        c.withColorFun(colorList) {
            getColor ->

            fun getCol(attr:Int) = getColor(attr)

            fun ImageView.setCol(attr:Int) = this.setColorFilter(getCol(attr),PorterDuff.Mode.SRC)
            fun TextView.setCol(attr:Int) = this.setTextColor(getCol(attr))
            fun ImageView.setColAtop(attr:Int) = this.setColorFilter(getCol(attr),PorterDuff.Mode.SRC_ATOP)

            color1.setColAtop(R.attr.table_header_text)
            slantedBar1.setColAtop(R.attr.tableView_headerColor)

            color2.setColAtop(android.R.attr.textColorPrimary)
            slantedBar2.setColAtop(R.attr.colorPrimary)

            color3.setColAtop(R.attr.button_plus_color)
            slantedBar3.setColAtop(R.attr.colorAccent)

            textView.setCol(android.R.attr.textColorTertiary)
            view.setBackgroundColor(getCol(R.attr.table_data_row_odd))
            slantedBarFill.setColAtop(R.attr.table_data_row_even)
        }

        textView.text = theme.colorThemeName
        return view
    }

}