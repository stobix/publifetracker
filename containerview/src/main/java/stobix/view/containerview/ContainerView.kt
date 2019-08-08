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
import stobix.utils.kotlinExtensions.folding
import stobix.utils.kotlinExtensions.map
import stobix.utils.kotlinExtensions.onFirst
import kotlin.math.max
import kotlin.math.min

@Suppress("NAME_SHADOWING")
open class ContainerView(ctx: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : View(ctx, attrs, defStyleAttr, defStyleRes) {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    private var isSetExternally = true

    //TODO rearrange how containers are drawn: 1 [smörgås] instead of [1 smörgås]

    /*
    The view needs to tell its parent that it needs to be redrawn if one of these
    properties gets changed externally.
    The update function handles this automagically.
     */

    var backColor = 0
        set(value) = update {
            field = value
            fillPaint.color = value
        }

    var textColor: Int = 0
        set(value) = update {
            field = value
            textPaint.color = value
        }

    var borderColor: Int = Color.WHITE
        set(value) = update {
            field = value
            strokePaint.color = value
        }

    var rectColorDark: Int = 0
        set(value) = update {
            field = value
            rectColorDarkPaint.color = value
        }

    var rectColorLight: Int = 0
        set(value) = update {
            field = value
            rectColorLightPaint.color = value
        }

    private val textView = TextView(context)

    /**
     *
     */
    var textSize = textView.textSize
        set(value) = update {
            if (value > 0f) {
                field = value
                textPaint.textSize = value
                textView.textSize = value
            }
        }

    var container: Container = Container()
        set(value) = update {
            field = value
            Log.d("ContainerView container", stringifyContainer())
        }

    /**
     *
     */
    var containerBorderWidth = 5f
        set(value) = update {
            field = value
        }


    private fun update(f: () -> Unit) {
        f()
        if (isSetExternally) {
            Log.d("ContainerView", "invalidating")
            invalidate()
        }
    }

    // properties set in f will not trigger invalidate()
    private fun setInternally(f: () -> Unit) {
        isSetExternally = false
        f()
        isSetExternally = true
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
    private fun paintLevel(current: Int, max: Int) = Paint().also { it.color = current * (rectColorLight - rectColorDark) / max }


    init {

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
                this.getColor(primary, getDef(secondary, fallback))

        setInternally {
            backColor = a.getColor(R.styleable.ContainerView_colorBackground, android.R.attr.colorPrimary, Color.WHITE)
            textColor = a.getColor(R.styleable.ContainerView_colorText, android.R.attr.textColorPrimary, Color.BLACK)
            rectColorDark = a.getColor(R.styleable.ContainerView_colorBorderDarkest, android.R.attr.colorAccent, Color.DKGRAY)
            rectColorLight = a.getColor(R.styleable.ContainerView_colorBorderLightest, android.R.attr.colorBackground, Color.LTGRAY)
            borderColor = a.getColor(R.styleable.ContainerView_colorBorder, android.R.attr.colorAccent, Color.WHITE)

            textSize = a.getDimension(R.styleable.ContainerView_textSize, TextView(context).textSize)
            Log.d("TEXT", "size: $textSize")
            textPaint.textSize = textSize

            container.addInt(3, "insulin")
            container.addString("godis", 3, "mums")
            val c1 = Container()
            c1.addString("mte", 1)
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

    }

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        fun getSize(spec: Int, desired: Int) = when (MeasureSpec.getMode(spec)) {
            MeasureSpec.EXACTLY -> "exactly" to MeasureSpec.getSize(spec)
            MeasureSpec.AT_MOST -> "at most" to min(MeasureSpec.getSize(spec), desired)
            else -> "desired" to desired
        }

        val wantedHeight = container.measureHeight(0 to countLayers())
        val wantedWidth = container.measureWidth(0 to countLayers())

        val (hdescr, height) = getSize(hSpec, wantedHeight.toInt())
        val (wdescr, width) = getSize(wSpec, wantedWidth.toInt())
        setMeasuredDimension(width, height)
        Log.d("ContainerView", "measure w $wdescr $width h $hdescr $height for ${stringifyContainer()}")
    }

    @ExperimentalUnsignedTypes
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("ContainerView draw", "====================================")
        Log.d("drawing", "${stringifyContainer()}")
        canvas.drawColor(backColor)

        fillStrokePaint.strokeWidth = 1f

        canvas.drawContainer(container, countLayers(container))
    }


    /**
     * The maximum recursion level to show for container containing containers
     */
    open var maxRecurLevel = 1
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
     * Show container contents down to level [maxRecurLevel]
     */
    open var showContents = false
        set(value) = update { field = value }
    /**
     * Show content description for recursive content before reaching [maxRecurLevel]
     */
    open var showContentDescriptions = false
        set(value) = update { field = value }

    private fun Any?.drawAt(level: Int) = if (maxRecurLevel >= level) this else null
    private fun Any?.inParens() = if (this != null) "($this)" else null
    private fun Any?.inBracks() = if (this != null) "[$this]" else null
    private fun Any?.inBraces() = if (this != null) "{$this}" else null
    private fun Any?.drawIf(b: Boolean) = if (b) this else null
    private fun Any?.drawOr(b: Boolean, that: Any?) = if (b) this else that

    private fun Any?.intersperce(that: Any?, chars: String) =
            if (this != null)
                if (that != null) "$this" + chars + "$that"
                else "$this"
            else
                if (that != null) "$that"
                else null

    private infix fun Any?.space(that: Any?) = this.intersperce(that, " ")
    private infix fun Any?.colon(that: Any?) = this.intersperce(that, ": ")
    private infix fun Any?.eq(that: Any?) = this.intersperce(that, " = ")
    private infix fun Any?.comma(that: Any?) = this.intersperce(that, ", ")

    private fun Container?.measureHeight(currRecurLevel: Pair<Int, Int> = 0 to maxRecurLevel): Float =
            this?.contents?.fold(2 * containerBorderWidth) { heightAcc, c ->
                2 * containerBorderWidth +
                        max(heightAcc,
                                when (c) {
                                    is IntContent -> c.measureHeight()
                                    is StringContent -> c.measureHeight()
                                    is ContainerContent -> c.measureHeight(currRecurLevel)
                                    else -> 0f
                                })
            } ?: 0f

    @Suppress("unused")
    private fun IntContent.measureHeight() = textSize

    @Suppress("unused")
    private fun StringContent.measureHeight() = textSize

    private fun ContainerContent.measureHeight(recurLevel: Pair<Int, Int>) =
            if (recurLevel.notMax())
                this.value.measureHeight(recurLevel onFirst { it + 1 })
            else
                textSize + 2 * containerBorderWidth

    /**
     * Returns the width of the container
     */
    private fun Container?.measureWidth(currRecurLevel: Pair<Int, Int> = 0 to maxRecurLevel): Float =
            this?.contents?.fold(0f) { acc, c ->
                acc + when (c) {
                    is IntContent -> c.measureWidth()
                    is StringContent -> c.measureWidth()
                    is ContainerContent -> c.measureWidth(currRecurLevel)//+2*containerBorderWidth
                    else -> 0f
                }
            } ?: 0f

    private fun IntContent.measureWidth() = textPaint.measureText(this.show()) + 2 * containerBorderWidth
    private fun StringContent.measureWidth() = textPaint.measureText(this.show()) + 2 * containerBorderWidth

    /**
     * @return false if the first value has not reached the second value (aka "max")
     */
    private fun Pair<Int, Int>.notMax() = this folding { a, b -> a < b }

    /**
     * Returns the width of the [ContainerContent] if drawn at the [recurLevel]
     */
    private fun ContainerContent.measureWidth(recurLevel: Pair<Int, Int>): Float =
            textPaint.measureText(this.showInit(recurLevel)) + 2 * containerBorderWidth +
                    if (recurLevel.notMax())
                        this.value.measureWidth(recurLevel onFirst { it + 1 })
                    else 0f

    private fun IntContent.show() =
            this.value space
            this.description.drawIf(showIntDescriptions) ?: ""

    private fun StringContent.show() =
            this.amount space
            this.value space
            this.description.inParens().drawIf(showStringDescriptions) ?: ""

    private fun ContainerContent.showInit(recurLevel: Pair<Int, Int>) =
            this.description?.let {
                (this.amount?.toString()?.plus(" x ") ?: "") to it
            }?.let { (amount, descr) ->
                if (recurLevel.notMax())
                    when {
                        showContentDescriptions -> "$amount$descr"
                        showContents -> amount
                        else -> ""
                    }
                else
                    when {
                        showContentDescriptions -> "$amount$descr"
                        showContents -> "$amount${descr}…"
                        else -> "$amount$descr"
                    }
            } ?: ""

    private fun indentationStr(recurLevel: Pair<Int, Int>) = (0..recurLevel.first).map { '\t' }.joinToString(separator = "")

    /**
     * Draws the container on the canvas, recursively. Returns the last x position of the drawing.
     */
    @ExperimentalUnsignedTypes
    private fun Canvas.drawContainer(container: Container?, maxRecurLevelAmount: Int, currRecurLevel: Int = 0, xPos: Float = 0f, yPos: Float = 0f, colors: Colour.ColorRange = Colour(rectColorDark)..Colour(rectColorLight) steps maxRecurLevel+1) =
            drawContainer(container, currRecurLevel to maxRecurLevelAmount, xPos, yPos, colors)

    private fun Canvas.drawContainer(container: Container?, recurLevel: Pair<Int, Int>, xPos: Float = 0f, yPos: Float = 0f, colors: Colour.ColorRange = Colour(rectColorDark)..Colour(rectColorLight) steps maxRecurLevel+1) =
            container?.also { Log.d("drawing", "${indentationStr(recurLevel)}>>>(container selection:$recurLevel)") }?.contents?.foldIndexed(xPos) { i, acc, c ->
                acc.also { } + when (c) {
                    is IntContent -> this.draw(c, acc, yPos, recurLevel)
                    is StringContent -> this.draw(c, acc, yPos, recurLevel)
                    is ContainerContent -> this.draw(c, acc, yPos, recurLevel, colors)
                    else -> 0f
                }
            }.also { Log.d("drawing", "${indentationStr(recurLevel)}<<<(container selection:$recurLevel) $xPos += $it") }

    /**
     * Draw the [IntContent] to the canvas. Returns the width of the drawn object
     */
    private fun Canvas.draw(content: IntContent, xPos: Float, yPos: Float, recurLevel: Pair<Int, Int>): Float {
        val string = content.show()
        Log.d("drawing", "${indentationStr(recurLevel)}>>>(int) $string ($xPos $yPos)")
        this.drawTextBetter(string, (xPos to yPos).map { it + containerBorderWidth }, recurLevel = recurLevel)
        val inc = content.measureWidth()
        return inc.also { Log.d("drawing", "${indentationStr(recurLevel)}<<<(int) $xPos += $it") }
    }

    /**
     * Draw the [StringContent] to the canvas. Returns the width of the drawn object
     */
    private fun Canvas.draw(content: StringContent, xPos: Float, yPos: Float, recurLevel: Pair<Int, Int>): Float {

        val string = content.show()
        Log.d("drawing", "${indentationStr(recurLevel)}>>>(string) $string ($xPos $yPos)")
        this.drawTextBetter(string, (xPos to yPos).map { it + containerBorderWidth }, recurLevel = recurLevel)
        val inc = content.measureWidth()
        return inc.also { Log.d("drawing", "${indentationStr(recurLevel)}<<<(string) $xPos += $it") }
    }

    /**
     * Draw the [ContainerContent] to the canvas. Returns the width of the drawn object
     */
    @ExperimentalUnsignedTypes
    private fun Canvas.draw(content: ContainerContent, xPos: Float, yPos: Float, recurLevel: Pair<Int, Int>,
                            colors: Colour.ColorRange): Float {
        val initString = content.showInit(recurLevel)
        val textWidth = textPaint.measureText(initString)
        val totalWidth = content.measureWidth(recurLevel)
        Log.d("drawing", "${indentationStr(recurLevel)}>>>(container:$recurLevel $initString ($xPos $yPos $totalWidth)")
        this.drawContainerBorder(
                initString, xPos, yPos,
                recurLevel,
                color = colors[recurLevel.first].color,
                containerWidth = totalWidth)
        if (recurLevel.notMax()) {
            also { Log.d("drawing", "${indentationStr(recurLevel)}===(container:$recurLevel=>${recurLevel onFirst { it + 1 }}) $xPos+=$textWidth") }
            this.drawContainer(content.value,
                    recurLevel onFirst { it + 1 },
                    xPos + textWidth,
                    yPos + containerBorderWidth,
                    colors = colors)
        }
        this.drawTextBetter(initString, (xPos to yPos).map { it + containerBorderWidth }, recurLevel = recurLevel)
        return totalWidth.also { Log.d("drawing", "${indentationStr(recurLevel)}<<<(container:$recurLevel) $xPos+=$it") }
    }


    /**
     * Draw a container border around some text, possibly with space for more contents
     * @param str the string to draw
     * @param color the [Color] to draw the container border in
     * @param xPos the start of the container for
    // * @param textWidth the width of the text (usually calculated automatically)
    // * @param borderWidth the width of the container border (usually calculated automatically)
     * @param extraWidth set this to allow some extra space after the text
    // * @param containerWidth the width of the whole drawn container. (usually calculated automatically)
     */
    private fun Canvas.drawContainerBorder(
            str: String, xPos: Float, yPos: Float
            , recurLevel: Pair<Int, Int>
            , color: Int,
            textWidth: Float = textPaint.measureText(str), extraWidth: Float = 0f, containerWidth: Float = textWidth + extraWidth + 2 * containerBorderWidth,
            containerHeight: Float = textSize + 2 * (recurLevel.second - recurLevel.first + 1) * containerBorderWidth
    ): Float {
        Log.d("drawing", "${indentationStr(recurLevel)}>>>(border) ($xPos $yPos $recurLevel)")
        val rectRect = Rect(xPos.toInt(), yPos.toInt(), (xPos + containerWidth).toInt(), (yPos + containerHeight).toInt())
        this.drawRect(rectRect, strokePaint.also { strokePaint.color })
        this.drawRect(rectRect, fillPaint.also { fillPaint.color = color })
        Log.d("drawing",
                "${indentationStr(recurLevel)}border: $str\t(x:$xPos\ty:$yPos\tw:$containerWidth,\th:$containerHeight\te:${xPos + containerWidth})")
        return containerWidth.also { Log.d("drawing", "${indentationStr(recurLevel)}<<<(border:) $xPos+=$it") }
    }

    /**
     * Draw the string with the upper left corner starting at [pos]
     * @param pos a pair with starting position x, starting position y as in [Canvas.drawTextBetter]
     */
    private fun Canvas.drawTextBetter(str: String, pos: Pair<Float, Float>, paint: Paint = textPaint, recurLevel: Pair<Int, Int>) =
            this.drawTextBetter(str, pos.first, pos.second, paint, recurLevel)

    /**
     * Draw the string with the upper left corner starting at [xPos] [yPos]
     * @param xPos starting position x
     * @param yPos starting position y, and not from the fucking baseline
     */
    private fun Canvas.drawTextBetter(str: String, xPos: Float, yPos: Float, paint: Paint = textPaint, recurLevel: Pair<Int, Int>) {
        val m = -textPaint.fontMetrics.top // because of course this is defined to be negative
        Log.d("drawing", "${indentationStr(recurLevel)}text: $str\t(x:$xPos\ty:$yPos\thd:$m,\tw:${textPaint.measureText(str)})")
        this.drawText(str, xPos, yPos + m, paint)
    }

    ////
    // Debug Functions
    ////

    private fun stringifyContainer(container: Container? = this.container, currRecurLevel: Int = 0): String? {
        var string = null as String?
        container ?: return ""
        for (c in container.contents)
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
                    val recurDesc =
                            if (maxRecurLevel > currRecurLevel)
                                if (showContents)
                                    c.description.drawIf(showContentDescriptions) eq stringifyContainer(c.value, currRecurLevel + 1).inBracks()
                                else
                                    c.description
                                            ?: stringifyContainer(c.value, currRecurLevel + 1).inBracks()
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
    private fun countLayers(container: Container? = this.container, currRecurLevel: Int = maxRecurLevel): Int =
            container?.contents?.map {
                when (it) {
                    is IntContent -> 1
                    is StringContent -> 1
                    is ContainerContent ->
                        if (currRecurLevel > 0 && showContents)
                            1 + countLayers(it.value, currRecurLevel - 1)
                        else
                            1

                    else -> 0
                }
            }?.reduce { x, y -> max(x, y) } ?: 0

}

