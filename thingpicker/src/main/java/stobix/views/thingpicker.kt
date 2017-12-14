package stobix.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

open class ThingPicker(context: Context,attrs: AttributeSet) : View(context, attrs) {

    private var isExternalSet = true

    var backColor = 0
        set(value) {
            field =value
            invalidateIfExternal()
        }
    var textColor: Int = 0
        set(value) {
            field =value
            invalidateIfExternal()
        }
    var rectColorDark: Int = 0
        set(value) {
            field =value
            invalidateIfExternal()
        }
    var rectColorLight: Int = 0
        set(value) {
            field =value
            invalidateIfExternal()
        }

    private fun rehashIfExternal(){
        if(isExternalSet) {
            invalidate()
            requestLayout()
        }
    }

    private fun invalidateIfExternal(){
        if(isExternalSet) {
            invalidate()
        }
    }

    private fun setInternally(f:()->Unit) {
        isExternalSet=false
        f()
        isExternalSet=true
    }

    val paint = Paint()

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ThingPicker,0,0)
        setInternally {
            backColor = a.getColor(R.styleable.ThingPicker_colorBackground, Color.WHITE)
            textColor = a.getColor(R.styleable.ThingPicker_colorText, Color.BLACK)
            rectColorDark = a.getColor(R.styleable.ThingPicker_colorBorderDarkest, Color.DKGRAY)
            rectColorLight = a.getColor(R.styleable.ThingPicker_colorBorderLightest, Color.LTGRAY)
        }
        a.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

}