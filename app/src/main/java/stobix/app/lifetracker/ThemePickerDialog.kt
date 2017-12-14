package stobix.app.lifetracker

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

class ThemePickerDialog
    (val ctx: Context, val colors: ArrayList<ThemeListItem>) : Dialog(ctx)
{
    override fun show() {
        super.show()
        this.setContentView(R.layout.theme_picker)
        this.setTitle(context.getString(R.string.theme_picker_dialog_title))
        val themesList = this.findViewById<ListView>(R.id.themePickerList)

        val adapter  = ArrayAdapter<ThemeListItem>(ctx,R.layout.theme_picker_text_view,R.id.themePickerTextView, ArrayList<ThemeListItem>())
        for(c in colors)
            adapter.add(c)
        themesList.adapter = adapter
        themesList.onItemClickListener = AdapterView.OnItemClickListener {
            adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            Log.d("click","$l, which is ${colors[l.toInt()]}")
            (ctx as MainActivity).doSetTheme(colors[l.toInt()].themeValue())
            dismiss()
        }

    }
}