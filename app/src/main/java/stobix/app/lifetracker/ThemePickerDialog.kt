package stobix.app.lifetracker

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView

/**
 * Created by JoelE on 2017-12-14.
 */
class ThemePickerDialog
    (val ctx: Context, val colors: ArrayList<ThemeListItem>) : Dialog(ctx)
{
    override fun show() {
        this.setTitle(context.getString(R.string.number_picker_sugar_level))
        this.setContentView(R.layout.theme_picker)
        val listView = this.findViewById<ListView>(R.id.themePickerList)
        for(c in colors)
            Log.d("colors",c.toString())
        //val adapter  = ArrayAdapter<ThemeListItem>(ctx,R.layout.theme_picker_text_view,colors)
        val adapter  = ArrayAdapter<ThemeListItem>(ctx,R.layout.theme_picker_text_view,ArrayList<ThemeListItem>())
        listView.adapter = adapter
        for(c in colors)
            adapter.add(c)
        super.show()
    }
}