package stobix.view.containerview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import stobix.utils.kotlin.Colour
import kotlin.math.max
import kotlin.math.min

open class ContainerView(ctx : Context, attrs: AttributeSet? = null, defStyleAttr: Int=0, defStyleRes: Int=0) : View(ctx, attrs,defStyleAttr,defStyleRes) {
    constructor(context: Context) : this(context,null)
    constructor(context: Context,attrs: AttributeSet? ) : this(context, attrs,0)
    constructor(context: Context,attrs: AttributeSet? = null, defStyleAttr: Int) : this(context, attrs, defStyleAttr,0)

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

    private val textView = TextView(context)

    var textSize = textView.textSize
        set(value) = update {
            if(value>0f) {
                field=value
                textPaint.textSize = value
                textView.textSize = value
            }
        }

    var container: Container = Container()
        set(value) = update {
            field = value
            Log.d("ContainerView container",stringifyContainer())
        }

    var containerWidth = 5f
        set(value) = update {
            field = value
        }


    private fun update (f:()->Unit) {
        f()
        if(isExternalSet) {
            Log.d("ContainerView","invalidating")
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
    private var strokePaint = Paint()
    private var fillPaint = Paint()
    private var fillStrokePaint = Paint()
    private var rectColorDarkPaint = Paint()
    private var rectColorLightPaint = Paint()
    /**
     * returns a paint object with the current paint level
     */
    private fun paintLevel(current: Int, max: Int) = Paint().also {it.color = current*(rectColorLight - rectColorDark)/max}


    init {
        Log.d("ContainerView init","mupp")

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ContainerView, defStyleAttr, defStyleRes)

        fun getDef(resource: Int, valueIfNotFound: Int = Color.MAGENTA): Int {
            val tv = TypedValue()
            context.theme.resolveAttribute(resource, tv, true)
            return if (tv.type >= TypedValue.TYPE_FIRST_COLOR_INT
                    && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                // resource is a color
                tv.data
            } else {
                valueIfNotFound
                // resource is not a color, probably a drawable
                //Drawable d = activity.getResources().getDrawable(a.resourceId);
            }
        }
        fun TypedArray.getColor(primary: Int, secondary: Int, fallback: Int) =
                this.getColor(primary,getDef(secondary,fallback))

        setInternally {
            backColor = a.getColor(R.styleable.ContainerView_colorBackground, android.R.attr.colorPrimary, Color.WHITE)
            textColor = a.getColor(R.styleable.ContainerView_colorText, android.R.attr.textColorPrimary, Color.BLACK)
            rectColorDark = a.getColor(R.styleable.ContainerView_colorBorderDarkest, android.R.attr.colorAccent, Color.DKGRAY)
            rectColorLight = a.getColor(R.styleable.ContainerView_colorBorderLightest, android.R.attr.colorBackground, Color.LTGRAY)
            // rectColorDark = Color.BLUE
            // rectColorLight = Color.YELLOW

            textSize = a.getDimension(R.styleable.ContainerView_textSize, TextView(context).textSize)
            Log.d("TEXT", "size: ${textPaint.textSize}")
            //textPaint.textSize = TextAppearance.size.toFloat()
            //Log.d("TEXT","size after: ${textPaint.textSize}")
            textPaint.textSize = textSize

            container.addInt(3, "insulin")
            container.addString("godis", 3, "mums")
            var c1 = Container()
            c1.addString("mte")
            c1.addContainer(
                    Container()
                            .addString("bröd", 1, "energibröd")
                            .addString("ost", 1, "cheddar")
                            .addString("smör", description = "bregott")
                    , 1
                    , "smörgås"
            )
            container.addContainer(c1, description = "frukost")
        }

        strokePaint.style = Paint.Style.STROKE
        fillPaint.style = Paint.Style.FILL
        fillStrokePaint.style = Paint.Style.FILL_AND_STROKE
        a.recycle()

        Log.d("ContainerView init",stringifyContainer())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private var measurement = 0 to 0


    override fun onMeasure(wSpec: Int, hSpec: Int) {
        fun getSize(spec:Int,desired: Int) = when(MeasureSpec.getMode(spec)){
            MeasureSpec.EXACTLY -> "exactly" to MeasureSpec.getSize(spec)
            MeasureSpec.AT_MOST -> "at most" to min(MeasureSpec.getSize(spec),desired)
            else -> "desired" to desired
        }

        val textView = TextView(context)
        textView.textSize = textSize
        textView.text = stringifyContainer()
        val layers = countLayers()
        Log.d("ContainerView","Layers $layers")
        // appearance.getDimensionPixelSize(attr, attributes.mTextSize);
        textView.measure(wSpec,hSpec)


        val (hdescr,height )= getSize(hSpec,(textSize+(2*containerWidth)*layers).toInt())
        val (wdescr,width) = getSize(wSpec,textView.measuredWidth)
        setMeasuredDimension(width,height)
        Log.d("ContainerView","measure $wdescr $width $hdescr $height for ${stringifyContainer()}")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d("ContainerView","attached ${stringifyContainer()}")
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d("ContainerView","layout ${stringifyContainer()}")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("ContainerView draw","text size: $textSize")
        canvas.drawColor(backColor)

        fillStrokePaint.strokeWidth = 1f

        for((i:Int,c: Colour) in (Colour(rectColorDark)..Colour(rectColorLight) steps countLayers()).withIndex() ) {
            val left = containerWidth*i
            val top = containerWidth*i
            val right = measuredWidth-containerWidth*i
            val bottom= measuredHeight-containerWidth*i
            Log.d("ContainerView draw","drawing rect: $left $top $right $bottom")
            canvas.drawRect( left, top, right, bottom, fillStrokePaint.also { fillStrokePaint.color = c.color })
            canvas.drawRect( left, top, right, bottom, strokePaint)
        }

        val drawThis = stringifyContainer(container) ?:""
        Log.d("ContainerView draw","draw this $drawThis")
        val textBounds = Rect()
        textPaint.getTextBounds(drawThis,0,drawThis.length,textBounds)
        canvas.drawText(drawThis,
                (countLayers()+1)*containerWidth,
                measuredHeight-(countLayers())*containerWidth-textBounds.bottom,
                textPaint)

        textPaint.textSize
    }


    /**
     * The maximum recursion level to show for container containing containers
     */
    open var recurLevel = 1
        set(value) = update { field = value }

    /**
     * Show descriptions for string content
     */
    open var showStringDescriptions = false
        set(value) = update { field = value }
    /**
     * Show descriptions for int content
     */
    open var showIntDescriptions = false
        set(value) = update { field = value }
    /**
     * Show container contents down to level [recurLevel]
     */
    open var showContents = false
        set(value) = update { field = value }
    /**
     * Show content description for recursive content before reaching [recurLevel]
     */
    open var showContentDescriptions = false
        set(value) = update { field = value }



    fun stringifyContainer() = stringifyContainer(this.container,this.recurLevel)

    fun stringifyContainer(container: Container?,currRecurLevel: Int=0):  String?{
        fun Any?.drawAt(level: Int) = if (recurLevel>=level) this else null
        fun Any?.inParens() = if(this!=null) "($this)" else null
        fun Any?.inBracks() = if(this!=null) "[$this]" else null
        fun Any?.inBraces() = if(this!=null) "{$this}" else null
        fun Any?.drawIf(b:Boolean) = if(b) this else null
        fun Any?.drawOr(b:Boolean,that: Any?) = if(b) this else that

        fun Any?.intersperce(that:Any?,chars: String) =
        if (this != null)
            if (that != null) "$this"+chars+"$that"
            else "$this"
        else
            if (that != null) "$that"
            else null

        infix fun Any?.space(that:Any?) = this.intersperce(that," ")
        infix fun Any?.colon(that:Any?) = this.intersperce(that,": ")
        infix fun Any?.equals(that:Any?) = this.intersperce(that," = ")
        infix fun Any?.comma(that:Any?) = this.intersperce(that,", ")
        var string = null as String?
        container ?: return ""
        for(c in container.contents)
            when (c) {
                is IntContent -> {
                    val intString = c.value space c.description.drawIf(showIntDescriptions)
                    string = string comma intString
                }
                is StringContent -> {
                    val stringString = c.amount space c.value space c.description.inParens().drawIf(showStringDescriptions)
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
                                c.description ?: ""
                    val containerString = c.amount space recurDesc
                    string = string comma containerString
                }
            }
        return string
    }

    /**
     * Counts the maximum amount of shown layers on the current recursion level
     */
    fun countLayers(container: Container?=this.container, currRecurLevel: Int=recurLevel):Int =
            container?.contents?.map {
                when (it) {
                    is IntContent -> 1
                    is StringContent -> 1
                    is ContainerContent ->
                        if (currRecurLevel > 0 && showContents)
                            1+countLayers(it.value, currRecurLevel - 1)
                        else
                            1

                    else -> 0
                }
            }?.reduce { x, y -> max(x,y)} ?: 0

}

