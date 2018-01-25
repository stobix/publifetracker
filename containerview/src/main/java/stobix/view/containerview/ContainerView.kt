package stobix.view.containerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View

open class ContainerView(context: Context,attrs: AttributeSet) : View(context, attrs) {

    private var isExternalSet = true

    /*
    The view needs to tell its parent that it needs to be redrawn if one of these
    properties gets changed externally.
    The update function handles this automagically.
     */

    var backColor = 0
        set(value) = update {
            field =value
            fillPaint.color=value
        }

    var textColor: Int = 0
        set(value) = update {
            field =value
            textPaint.color=value
        }

    var rectColorDark: Int = 0
        set(value) = update {
            field =value
            rectColorDarkPaint.color=value
        }

    var rectColorLight: Int = 0
        set(value) = update {
                field =value
                rectColorLightPaint.color=value
            }

    var textSize = -1f
        set(value) = update {
            field=value
            if(value>0f)
                textPaint.textSize = value
        }

    var container: Container = Container()
        set(value) = update {
            field = value
        }


    private fun update (f:()->Unit) {
        f()
        if(isExternalSet) {
            invalidate()
        }
    }

    // properties set in f will not trigger invalidate()
    private fun setInternally(f:()->Unit) {
        isExternalSet=false
        f()
        isExternalSet=true
    }



    private var textPaint = Paint()
    private var fillPaint = Paint()
    private var rectColorDarkPaint = Paint()
    private var rectColorLightPaint = Paint()

    init {
        Log.d("ContainerView init","mupp")
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ContainerView,0,0)


        fun getDefColor(resource: Int,valueIfNotFound: Int = Color.MAGENTA) : Int {
            val tv = TypedValue()
            context.theme.resolveAttribute(resource, tv, true)
            return if (tv.type >= TypedValue.TYPE_FIRST_COLOR_INT
                    && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                // windowBackground is a color
                tv.data
            } else {
                valueIfNotFound
                // windowBackground is not a color, probably a drawable
                //Drawable d = activity.getResources().getDrawable(a.resourceId);
            }
        }

        setInternally {
            backColor = a.getColor(R.styleable.ContainerView_colorBackground,
                    getDefColor(android.R.attr.colorPrimary,Color.WHITE))
            textColor = a.getColor(R.styleable.ContainerView_colorText,
                   getDefColor(android.R.attr.textColorPrimary,Color.BLACK))
            rectColorDark = a.getColor(R.styleable.ContainerView_colorBorderDarkest,
                    getDefColor(android.R.attr.colorAccent,Color.DKGRAY))
            rectColorLight = a.getColor(R.styleable.ContainerView_colorBorderLightest,
                    getDefColor(android.R.attr.colorBackground, Color.LTGRAY))

            textSize = a.getDimension(R.styleable.ContainerView_textSize,20f)
            Log.d("TEXT","size: ${textPaint.textSize}")
            //textPaint.textSize = TextAppearance.size.toFloat()
            //Log.d("TEXT","size after: ${textPaint.textSize}")
            textPaint.textSize = textSize

            container.addInt(3,"insulin")
            container.addString("godis",3,"mums")
            var c1 = Container()
            c1.addString("mte")
            c1.addContainer(
                    Container()
                            .addString("bröd",1,"energibröd")
                            .addString("ost",1,"cheddar")
                            .addString("smör",description="bregott")
                    ,1
                    ,"smörgås"
            )
            container.addContainer(c1,description = "frukost")
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
        val drawThis = stringifyContainer(container)
        Log.d("ContainerView draw","draw this $drawThis")
        canvas.drawText(drawThis?:"",10f,textSize,textPaint)
    }

    open var recurLevel = 1
        set(value) = update { field = value }

    open var showDescriptions = false
        set(value) = update { field = value }
    open var showTags = false
        set(value) = update { field = value }
    open var showContents = false
        set(value) = update { field = value }
    open var showContentDescriptions = false
        set(value) = update { field = value }

    private fun Any?.drawAt(level: Int) = if (recurLevel>=level) this else null
    private fun Any?.inParens() = if(this!=null) "($this)" else null
    private fun Any?.inBracks() = if(this!=null) "[$this]" else null
    private fun Any?.inBraces() = if(this!=null) "{$this}" else null
    private fun Any?.drawIf(b:Boolean) = if(b) this else null
    private fun Any?.drawOr(b:Boolean,that: Any?) = if(b) this else that

    private fun Any?.intersperce(that:Any?,chars: String) =
            if (this != null)
                if (that != null) "$this"+chars+"$that"
                else "$this"
            else
                if (that != null) "$that"
                else null

    private infix fun Any?.space(that:Any?) = this.intersperce(that," ")
    private infix fun Any?.colon(that:Any?) = this.intersperce(that,": ")
    private infix fun Any?.equals(that:Any?) = this.intersperce(that," = ")
    private infix fun Any?.comma(that:Any?) = this.intersperce(that,", ")

    fun stringifyContainer(container: Container?,currRecurLevel: Int=0):  String?{
        var string = null as String?
        container ?: return ""
        for(c in container.contents) when (c) {
            is IntContent -> {
                val intString = c.value space c.description.drawIf(showTags)
                string = string comma intString
            }
            is StringContent -> {
                val stringString = c.amount space c.value space c.description.inParens().drawIf(showDescriptions)
                string = string comma stringString
            }
            is ContainerContent -> {
                // TODO only show description if necessary unless the user has put on some verbose thing
                val recurDesc =
                        if (recurLevel > currRecurLevel)
                            if (showContents)
                                c.description.drawIf(showContentDescriptions) equals stringifyContainer(c.value,currRecurLevel+1).inBracks()
                            else
                                c.description ?: stringifyContainer(c.value,currRecurLevel+1).inBracks()
                        else
                            c.description
                val containerString = c.amount space recurDesc
                string = string comma containerString
            }
        }
        return string
    }

}