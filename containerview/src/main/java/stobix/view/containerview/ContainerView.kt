package stobix.view.containerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View

open class ContainerView(context: Context,attrs: AttributeSet) : View(context, attrs) {

    private var isExternalSet = true

    var backColor = 0
        set(value) {
            field =value
            fillPaint.color=value
            invalidateIfExternal()
        }
    var textColor: Int = 0
        set(value) {
            field =value
            textPaint.color=value
            invalidateIfExternal()
        }
    var rectColorDark: Int = 0
        set(value) {
            field =value
            rectColorDarkPaint.color=value
            invalidateIfExternal()
        }
    var rectColorLight: Int = 0
        set(value) {
            field =value
            rectColorLightPaint.color=value
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
    var textSize = -1f
        set(value) {
            field=value
            if(value>0f)
                textPaint.textSize = value
            invalidateIfExternal()
        }

    private var textPaint = Paint()
    private var fillPaint = Paint()
    private var rectColorDarkPaint = Paint()
    private var rectColorLightPaint = Paint()

    init {
        Log.d("ContainerView init","mupp")
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ContainerView,0,0)

        setInternally {
            backColor = a.getColor(R.styleable.ContainerView_colorBackground,
                    //ContextCompat.getColor(context, R.color.colorPrimary)
                    Color.WHITE
            )
            textColor = a.getColor(R.styleable.ContainerView_colorText, Color.BLACK)
            rectColorDark = a.getColor(R.styleable.ContainerView_colorBorderDarkest, Color.DKGRAY)
            rectColorLight = a.getColor(R.styleable.ContainerView_colorBorderLightest, Color.LTGRAY)
            if (textSize==-1f)
                textSize = textPaint.textSize
            else
                textPaint.textSize = textSize
        }

        fillPaint.style=Paint.Style.FILL
        a.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("ContainerView draw","text size: $textSize")
        canvas.drawColor(backColor)
        canvas.drawText("test!",10f,10f,textPaint)
    }


}