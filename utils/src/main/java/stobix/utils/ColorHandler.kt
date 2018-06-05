package stobix.utils

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.support.annotation.StyleableRes
import android.support.v4.content.ContextCompat
import android.view.ContextThemeWrapper

class ColorHandler(val ctx: Context) {
    var themeRes: Int? = null
    var missingColor = Color.MAGENTA

    val theme: Resources.Theme
        get()  {
            val t = themeRes
            return if (t != null) {
                val wrapper = ContextThemeWrapper(ctx,t)
                wrapper.theme
            } else {
                ctx.theme
            }
        }

    fun withStyleable(styleableRes: Int, f:(Map<Int,Int>)) {
        // TODO: make this similar to withColors, but using a styleable res instead
        // theme.obtainStyledAttributes(sortedColors).also { ...?

    }

    fun withColors(colorList:List<Int>, f: (Map<Int,Int>) -> Unit) {
        val sortedColors = colorList.sorted().toIntArray()
        theme.obtainStyledAttributes(sortedColors).also {
            val colorMap = colorList.map { colorRes ->
                val colorVal =
                        it.getColor(
                                it.getIndex(
                                        sortedColors.indexOf(
                                                colorRes)
                                ),
                                missingColor)
                colorRes to colorVal
            }.toMap()
            f(colorMap)
        }.recycle()
    }
    fun withThemeColors(theme: Int ){
    }

}