package stobix.app.lifetracker

import android.app.Dialog
import android.content.Context
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class ThemePickerDialog
    (private val ctx: Context, private val themes: ArrayList<ThemeListItem>) : Dialog(ctx)
{
    override fun show() {
        super.show()
        this.setContentView(R.layout.theme_picker)
        this.setTitle(context.getString(R.string.theme_picker_dialog_title))
        val themesList = this.findViewById<ListView>(R.id.themePickerList)

        val adapter  = ThemeArrayAdapter(ctx, ArrayList())
        for(t in themes)
            adapter.add(t)
        themesList.adapter = adapter
        themesList.onItemClickListener = AdapterView.OnItemClickListener {
            _: AdapterView<*>, _: View, _viewedRow: Int, index: Long ->
            Log.d("click","$index, which is ${themes[index.toInt()]}")
            (ctx as MainActivity).doSetTheme(themes[index.toInt()].getThemeResourceValue())
            dismiss()
        }

    }
}

class ThemeArrayAdapter(ctx: Context,  items: ArrayList<ThemeListItem>)
    :
        ArrayAdapter<ThemeListItem>(ctx,R.layout.theme_picker_item_view,items)
{
    val layout = R.layout.theme_picker_item_view

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val theme = getItem(position)
        val view = convertView ?: LayoutInflater.from(context)!!.inflate(layout,parent,false)
        val colorPrimary = view.findViewById<ImageView>(R.id.themeViewPrimaryColor)
        val colorAccent = view.findViewById<ImageView>(R.id.themeViewAccentColor)
        val colorPrimaryDark = view.findViewById<ImageView>(R.id.themeViewPrimaryDark)
        val textView = view.findViewById<TextView>(R.id.themeViewText)
        colorPrimary.setColorFilter(theme.color1,PorterDuff.Mode.SRC)
        colorAccent.setColorFilter(theme.color2,PorterDuff.Mode.SRC)
        colorPrimaryDark.setColorFilter(theme.color3,PorterDuff.Mode.SRC)
        /*
        colorPrimaryDark.setColorFilter(
                ResourcesCompat.getColor(
                        ctx.resources,
                        android.R.color.primary_text_dark,
                        R.style.Theme_Cotton <-- TODO fix this later for automagic styling; How to get style info from an R.style?
                ),
                PorterDuff.Mode.SRC)
                */
        textView.setText(theme.colorThemeName)
        textView.setTextColor(theme.textcolor)
        view.setBackgroundColor(theme.backgroundcolor)
        return view
    }

}