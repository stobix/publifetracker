package stobix.app.lifetracker

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import stobix.utils.ColorHandler

class ColorEditorActivity : Activity(), AdapterView.OnItemSelectedListener {
    fun setColors(themeId: Int){
        var c = ColorHandler(applicationContext)
        c.themeRes = themeId
        c.withColorFun(ColorsMeta.colorsUsed) { getColor ->
            fun TextView.color(id: Int) : Int {
                val color = getColor(id)
                // Log.d("ColorEditor","color $color")
                this.setTextColor(color)
                return color
            }
            fun TextView.backColor(id: Int) : Int {
                val color = getColor(id)
                // Log.d("ColorEditor","backcolor $color")
                this.setBackgroundColor(color)
                return color
            }
            fun TextView.colors(id: Int, bid: Int){
                this.color(id)
                this.backColor(bid)
            }
            infix fun TextView.withColor(id: Int): TextView{
                this.color(id)
                return this
            }
            infix fun TextView.withBack(id: Int): TextView{
                this.backColor(id)
                return this
            }
            val topBar = findViewById<TextView>(R.id.colorEditTopBar)
            val menuBar = findViewById<TextView>(R.id.colorEditMenuBar)
            val tableHeader = findViewById<TextView>(R.id.colorEditTableHeader)
            val tableRowEven = findViewById<TextView>(R.id.colorEditTableRowEven)
            val tableRowOdd = findViewById<TextView>(R.id.colorEditTableRowOdd)
            val buttonRow = findViewById<TextView>(R.id.colorEditButtonRow)
            topBar          withBack R.attr.colorPrimaryDark
            menuBar         withColor android.R.attr.textColorPrimary       withBack R.attr.colorPrimary
            tableHeader     withColor R.attr.table_header_text              withBack R.attr.tableView_headerColor
            tableRowEven    withColor android.R.attr.textColorTertiary      withBack R.attr.table_data_row_even
            tableRowOdd     withColor android.R.attr.textColorTertiary      withBack R.attr.table_data_row_odd
            buttonRow       withBack android.R.attr.windowBackground
            // tableRowEven    withColor R.attr.table_data_text    withBack R.attr.table_data_row_even
            // tableRowOdd     withColor R.attr.table_data_text    withBack R.attr.table_data_row_odd
        }
        // "12,2133,423-432,47-59".split(",").partition { it.contains("-") }.run{this.first.flatMap{it.split("-").map{it.toInt()}.run{(this[0]..this[1]).asSequence().toList()}}+this.second.map{it.toInt()}}.sorted().distinct()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_color_editor)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val picker = findViewById<Spinner>(R.id.colorEditThemePicker)

        picker.adapter = ThemeArrayAdapter(this,MainActivity.COLOR_THEMES).also{
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        picker.onItemSelectedListener = this
        setColors(R.style.Hjul)
        // var img = findViewById<ImageView>(R.id.colorEditorPreviewImage);
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        setColors(MainActivity.COLOR_THEMES[position].themeResourceValue)
    }

}