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

    fun withColorMap(colorList:List<Int>, f: (Map<Int,Int>) -> Unit) {
        val sortedColors = colorList.sorted().toIntArray()
        withColorMap(sortedColors,f)
    }

    fun withColorMap(sortedColorArray: IntArray, f: (Map<Int,Int>) -> Unit) {
        theme.obtainStyledAttributes(sortedColorArray).also {
            val colorMap = sortedColorArray.map { colorRes ->
                val colorVal =
                        it.getColor(
                                it.getIndex(
                                        sortedColorArray.indexOf(
                                                colorRes)
                                ),
                                missingColor)
                colorRes to colorVal
            }.toMap()
            f(colorMap)
        }.recycle()
    }

    fun withColorFun(colorList:List<Int>, f: ((Int) -> Int) -> Unit) {
        val sortedColors = colorList.sorted().toIntArray()
        withColorFun(sortedColors,f)
    }

    fun withColorFun(colorList:IntArray, f: ((Int) -> Int) -> Unit) {
        var functionValid = true
        theme.obtainStyledAttributes(colorList)
                .also {
                    fun getColor(color: Int): Int{
                        if(!functionValid)
                            error("function called outside its withColorFun loop")
                        return it.getColor(
                                it.getIndex(
                                        colorList.indexOf(color)),
                                missingColor)
                    }
                    f(::getColor)
                }
                .also{
                    functionValid = false
                }.recycle()
    }

    fun withDefColorFun(colorList:List<Int>, f: ((Int,Int) -> Int) -> Unit) {
        val sortedColors = colorList.sorted().toIntArray()
        withDefColorFun(sortedColors,f)
    }

    fun withDefColorFun(colorList:IntArray, f: ((Int,Int) -> Int) -> Unit) {
        var functionValid = true
        theme.obtainStyledAttributes(colorList)
                .also {
                    fun getColor(color: Int, defColor: Int): Int{
                        if(!functionValid)
                            error("function called outside its withColorFun loop")
                        return it.getColor(
                                it.getIndex(colorList.indexOf(color)),
                                it.getColor(
                                        it.getIndex(colorList.indexOf(defColor)),
                                        missingColor))
                    }
                    f(::getColor)
                }
                .also{
                    functionValid = false
                }.recycle()
    }


    fun withThemeColors(theme: Int ){
    }

}