package stobix.app.lifetracker

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

class ThemePickerDialog
    (private val ctx: Context, private val colors: ArrayList<ThemeListItem>) : Dialog(ctx)
{
    override fun show() {
        super.show()
        this.setContentView(R.layout.theme_picker)
        this.setTitle(context.getString(R.string.theme_picker_dialog_title))
        val themesList = this.findViewById<ListView>(R.id.themePickerList)

        val adapter  = ArrayAdapter<ThemeListItem>(
                ctx,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                ArrayList<ThemeListItem>())
        for(c in colors)
            adapter.add(c)
        themesList.adapter = adapter
        themesList.onItemClickListener = AdapterView.OnItemClickListener {
            _: AdapterView<*>, _: View, _viewedRow: Int, index: Long ->
            Log.d("click","$index, which is ${colors[index.toInt()]}")
            (ctx as MainActivity).doSetTheme(colors[index.toInt()].themeValue())
            dismiss()
        }

    }
}