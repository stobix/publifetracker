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
import stobix.utils.kotlinExtensions.to
import kotlin.math.max
import kotlin.math.min
import kotlin.Float as XCoord
import kotlin.Float as YCoord
import kotlin.Float as TextYCoord
import kotlin.Float


/**
 * A View to draw recursive [Container]s
 */
@Suppress("NAME_SHADOWING")
open class ContainerView(ctx: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : View(ctx, attrs, defStyleAttr, defStyleRes) {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    private var isSetExternally = true

    /*
    The view needs to tell its parent that it needs to be redrawn if one of these
    properties gets changed externally.
    The update function handles this automagically.
     */

    /**
     * The background color of the canvas that [Container]s are drawn on
     */
    var backColor = 0
        set(value) = update {
            field = value
            fillPaint.color = value
        }

    /**
     * The color of the container text
     */
    var textColor: Int = 0
        set(value) = update {
            field = value
            textPaint.color = value
            smallTextPaint.color = value
        }

    /**
     * Border color for the [Container]s
     */
    var borderColor: Int = Color.WHITE
        set(value) = update {
            field = value
            strokePaint.color = value
        }

    /**
     * The outermost background color for recursive containers
     */
    var outermostRectColor: Int = 0
        set(value) = update {
            field = value
            rectColorDarkPaint.color = value
        }

    /**
     * The innermost background color for recursive containers
     */
    var innermostRectColor: Int = 0
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
                smallTextPaint.textSize = value * 2 / 3
                textView.textSize = value
            }
        }

    /**
     * The [Container] to draw on this [ContainerView]
     */
    var container: Container = Container()
        set(value) = update {
            field = value
            Log.d("ContainerView container", stringifyContainer())
        }

    /**
     * The margin from the container edge to its inner elements
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
    private var smallTextPaint = Paint()
    private var strokePaint = Paint()
    private var fillPaint = Paint()
    private var fillStrokePaint = Paint()
    private var rectColorDarkPaint = Paint()
    private var rectColorLightPaint = Paint()
    /**
     * returns a paint object with the current paint level
     */
    private fun paintLevel(current: Int, max: Int) = Paint().also { it.color = current * (innermostRectColor - outermostRectColor) / max }


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
            outermostRectColor = a.getColor(R.styleable.ContainerView_colorBorderDarkest, android.R.attr.colorAccent, Color.DKGRAY)
            innermostRectColor = a.getColor(R.styleable.ContainerView_colorBorderLightest, android.R.attr.colorBackground, Color.LTGRAY)
            borderColor = a.getColor(R.styleable.ContainerView_colorBorder, android.R.attr.colorAccent, Color.WHITE)

            textSize = a.getDimension(R.styleable.ContainerView_textSize, TextView(context).textSize)
            Log.d("TEXT", "size: $textSize")
            textPaint.textSize = textSize
            smallTextPaint.textSize = textSize * 2 / 3

            //container.addInt(3, "insulin")
            //container.addString("godis", 3, "mums")
            val c1 = Container()
            c1.addString("kaka", 1)
            c1.addContainer(Container().addString("te", description = "normalstor").addString("mjölk"), 1, "mte")
            c1.addContainer(
                    Container()
                            .addContainer(
                                    Container()
                                            .addString("bröd", 1, "energibröd")
                                            .addString("smör", description = "bregott"),
                                    description = "senergi"
                            )
                            .addString("ostskiva", 3, "cheddar")
                    , 2
                    , "smörgås"
            )
            container.addContainer(c1, description = "frukost")
        }

        strokePaint.style = Paint.Style.STROKE
        fillPaint.style = Paint.Style.FILL
        fillStrokePaint.style = Paint.Style.FILL_AND_STROKE
        a.recycle()

    }

    @Suppress("KDocMissingDocumentation")
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

    @Suppress("KDocMissingDocumentation")
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
     * Whether to show amounts for [ContainerContent] and [StringContent] below 2.
     * If set to true, only null values are not shown.
     */
    open var showAmountsBelowTwo = false
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
     * Whether to show container contents down to level [maxRecurLevel].
     * Setting this false has the same effect as setting [maxRecurLevel] = 0
     */
    open var showContents = true
        set(value) = update { field = value }

    /**
     * The maximum recursion level to show for container containing containers
     */
    open var maxRecurLevel = 1
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

    private fun Pair<Int, Int>.notNextMax() = this folding { a, b -> a + 1 < b }

    /**
     * Returns the width of the [ContainerContent] if drawn at the [recurLevel]
     */
    private fun ContainerContent.measureWidth(recurLevel: Pair<Int, Int>): Float =
            this.run {
                containerBorderWidth +
                        measureAmount(recurLevel) +
                        measureDescription(recurLevel) +
                        measureContainer(recurLevel) +
                        containerBorderWidth
            }

    private fun ContainerContent.measureDescription(recurLevel: Pair<Int, Int>) =
            (this.description?.let {
                (if (recurLevel.notMax()) smallTextPaint else textPaint)
                        .measureText(this.showInit(recurLevel)).let {
                            if (it > 0f)
                                it + 2 * containerBorderWidth
                            else 0f
                        }
            } ?: 0f).also { Log.d("measure", "${indentationStr(recurLevel)}${this.description}: $it") }

    private fun ContainerContent.measureAmount(recurLevel: Pair<Int, Int>) =
            this.amount
                    .ifAboveTreshold(active = !showAmountsBelowTwo)
                    ?.let {
                        textPaint.measureText(this.showAmount(recurLevel)) + 2 * containerBorderWidth
                    } ?: 0f

    private fun ContainerContent.measureContainer(recurLevel: Pair<Int, Int>) =
            if (recurLevel.notMax())
                this.value.measureWidth(recurLevel onFirst { it + 1 }) +
                        ((this.value?.contents?.size ?: 1) - 1) * textPaint.measureText(",")
            else 0f


    /**
     * "Show" the contents akin to Haskell's Show class
     */
    private fun IntContent.show() =
            this.value.ifAboveTreshold(active = !showAmountsBelowTwo) space
            this.description.drawIf(showIntDescriptions) ?: ""

    /**
     * "Show" the contents akin to Haskell's Show class
     */
    private fun StringContent.show() =
            this.amount.ifAboveTreshold(active = !showAmountsBelowTwo) space
            this.value space
            this.description.inParens().drawIf(showStringDescriptions) ?: ""


    private fun ContainerContent.showAmount(recurLevel: Pair<Int, Int>) =
            this.amount.ifAboveTreshold(active = !showAmountsBelowTwo)
                    ?.toString()
                    ?.let {
                        if (recurLevel.notMax())
                            when {
                                showContentDescriptions -> it
                                showContents -> it
                                else -> ""
                            }
                        else
                            it
                    } ?: ""

    private fun Int?.ifAboveTreshold(treshold: Int = 1, active: Boolean = true) =
            this?.let { if ( !active || it > treshold ) it else null }

    /**
     * "Show" the contents akin to Haskell's Show class
     */
    private fun ContainerContent.showInit(recurLevel: Pair<Int, Int>) =
            this.description?.let {
                if (recurLevel.notMax())
                    when {
                        showContentDescriptions -> it
                        showContents -> ""
                        else -> ""
                    }
                else
                    when {
                        showContentDescriptions -> it
                        showContents -> "${it}…"
                        else -> it
                    }
            } ?: ""

    private fun indentationStr(recurLevel: Pair<Int, Int>) = (0..recurLevel.first).map { '\t' }.joinToString(separator = "")

    /**
     * Draws the container on the canvas, recursively. Returns the last x position of the drawing.
     */
    @ExperimentalUnsignedTypes
    private fun Canvas.drawContainer(
            container: Container?,
            maxRecurLevelAmount: Int,
            currRecurLevel: Int = 0,
            xPos: Float = 0f,
            yPos: Float = 0f,
            colors: Colour.ColorRange = Colour(outermostRectColor)..Colour(innermostRectColor) steps maxRecurLevel + 1
    ) {

        val textYPos = countLayers(container)*containerBorderWidth
        drawContainer(container, currRecurLevel to maxRecurLevelAmount, xPos to yPos to textYPos, colors)
    }

    private fun Canvas.drawContainer(container: Container?, recurLevel: Pair<Int, Int>, pos: Triple<XCoord, YCoord, TextYCoord>, colors: Colour.ColorRange): Float {
        if (container?.contents == null) {
            return 0f
        } else {
            Log.d("drawing", "${indentationStr(recurLevel)}>>>(container selection:$recurLevel)")
            val iMax = container.contents.size
            val nextPos = container.contents.foldIndexed(pos.first) { i, acc, c ->
                (acc + when (c) {
                    is IntContent -> this.draw(c, pos.onFirst { acc }, recurLevel)
                    is StringContent -> this.draw(c, pos.onFirst { acc }, recurLevel)
                    is ContainerContent -> this.draw(c, pos.onFirst { acc }, recurLevel, colors)
                    else -> 0f
                }).let {
                    it + if (i < iMax - 1)
                        this.drawTextBetter(",",  it to pos.third ,  recurLevel = recurLevel)
                    else 0f
                }
            }
            Log.d("drawing",
                    "${indentationStr(recurLevel)}<<<" +
                            "(container selection:$recurLevel) $pos += ${nextPos to 0}")
            return nextPos
        }
    }

    /**
     * Draw the [IntContent] to the canvas. Returns the width of the drawn object
     * @param content the [StringContent] to draw
     * @param pos the start upper left corner of the container
     * @param recurLevel the current recursion level
     */
    private fun Canvas.draw(content: IntContent, pos: Triple<Float, Float, Float>, recurLevel: Pair<Int, Int> ): Float {
        val string = content.show()
        Log.d("drawing", "${indentationStr(recurLevel)}>>>(int) $string $pos")
        this.drawTextBetter(string, addBW(pos.first) to pos.third, recurLevel = recurLevel)
        val inc = content.measureWidth()
        Log.d("drawing", "${indentationStr(recurLevel)}<<<(int) $pos += ${inc to 0}")
        return inc
    }

    /**
     * Draw the [StringContent] to the canvas. Returns the width of the drawn object
     * @param content the [StringContent] to draw
     * @param pos the start upper left corner of the container
     * @param recurLevel the current recursion level
     */
    private fun Canvas.draw(content: StringContent, pos: Triple<Float, Float, Float>, recurLevel: Pair<Int, Int>): Float {
        val string = content.show()
        Log.d("drawing", "${indentationStr(recurLevel)}>>>(string) $string $pos")
        drawTextBetter(string, addBW(pos.first) to pos.third, recurLevel = recurLevel)
        val inc = content.measureWidth()
        Log.d("drawing", "${indentationStr(recurLevel)}<<<(string) $pos += ${inc to 0}")
        return inc
    }

    /**
     * Draw the [ContainerContent] to the canvas. Returns the width of the drawn object
     * @param content the [ContainerContent] to draw
     * @param pos the start upper left corner of the container
     * @param recurLevel the current recursion level
     * @param colors the range of colors to use
     */
    @ExperimentalUnsignedTypes
    private fun Canvas.draw(content: ContainerContent, pos: Triple<Float, Float, Float>, recurLevel: Pair<Int, Int>,
                            colors: Colour.ColorRange): Float {
        with(content) {
            val amountString = showAmount(recurLevel)
            val initString = showInit(recurLevel)
            val initWidth = measureDescription(recurLevel)
            val totalWidth = measureWidth(recurLevel)
            Log.d("drawing", "${indentationStr(recurLevel)}>>>(container:$recurLevel $initString ($pos $totalWidth)")
            val textY = pos.third
            var drawPos = (pos.first to pos.second).map(::addBW)
            var textPos = drawPos.first to textY
            drawTextBetter(amountString, textPos, recurLevel = recurLevel)

            drawPos = drawPos onFirst { it + measureAmount(recurLevel) }
            textPos = textPos onFirst {drawPos.first}
            drawContainerBorder(
                    initString, drawPos,
                    recurLevel,
                    color = colors[recurLevel.first].color,
                    containerWidth = measureContainer(recurLevel) + measureDescription(recurLevel)
            )

            if(recurLevel.notMax()){
                drawTextBetter(initString, drawPos onFirst ::addBW, recurLevel = recurLevel, paint = smallTextPaint )
            } else {
                drawTextBetter(initString, textPos onFirst ::addBW, recurLevel = recurLevel, paint = textPaint)
            }


            if (recurLevel.notMax()) {
                Log.d("drawing",
                        "${indentationStr(recurLevel)}===(container:$recurLevel=>${recurLevel onFirst { it + 1 }}) " +
                                "$pos+=${initWidth to containerBorderWidth}")
                drawContainer(content.value,
                        recurLevel onFirst { it + 1 },
                        drawPos onFirst { it + initWidth } to textY,
                        colors = colors)
            }
            Log.d("drawing", "${indentationStr(recurLevel)}<<<(container:$recurLevel) $pos+=${totalWidth to 0}")
            return totalWidth
        }
    }

    private fun addBW(f: Float) = f + containerBorderWidth


    /**
     * Draw a container border around some text, possibly with space for more contents
     * @param str the string to draw
     * @param color the [Color] to draw the container border in
     * @param pos the start upper left corner of the container
    // * @param textWidth the width of the text (usually calculated automatically)
    // * @param borderWidth the width of the container border (usually calculated automatically)
     * @param extraWidth set this to allow some extra space after the text
    // * @param containerWidth the width of the whole drawn container. (usually calculated automatically)
     */
    private fun Canvas.drawContainerBorder(
            str: String, pos: Pair<Float, Float>,
            recurLevel: Pair<Int, Int>,
            color: Int,
            textWidth: Float = textPaint.measureText(str),
            extraWidth: Float = 0f,
            containerWidth: Float = textWidth + extraWidth + 2 * containerBorderWidth,
            containerHeight: Float = textSize + 2 * (recurLevel.second - recurLevel.first + 1) * containerBorderWidth
    ): Float {
        val (xPos, yPos) = pos.map { it.toInt() }
        Log.d("drawing", "${indentationStr(recurLevel)}>>>(border) ($xPos $yPos $recurLevel)")
        val rectRect = Rect(xPos, yPos, xPos + containerWidth.toInt(), yPos + containerHeight.toInt())
        this.drawRect(rectRect, strokePaint.also { strokePaint.color })
        this.drawRect(rectRect, fillPaint.also { fillPaint.color = color })
        Log.d("drawing",
                "${indentationStr(recurLevel)}border: $str\t(x:$xPos\ty:$yPos\tw:$containerWidth,\th:$containerHeight\te:${xPos + containerWidth})")

        Log.d("drawing", "${indentationStr(recurLevel)}<<<(border:) $pos+=${containerWidth to 0}")
        return containerWidth
    }

    /**
     * Draw the string with the upper left corner starting at [pos]
     * @param pos the coordinate of the upper left (x,y) corner of the text to be drawn
     * @param paint the [Paint] to draw the text with
     * @param recurLevel for debugging purposes
     */
    private fun Canvas.drawTextBetter(str: String, pos: Pair<Float, Float>, paint: Paint = textPaint, recurLevel: Pair<Int, Int>) :Float {
        val m = -paint.fontMetrics.top // because of course this is defined to be negative
        val (xPos, yPos) = pos
        Log.d("drawing", "${indentationStr(recurLevel)}text: $str\t(x:$xPos\ty:$yPos\thd:$m,\tw:${paint.measureText(str)})")
        this.drawText(str, xPos, yPos + m, paint)
        return paint.measureText(str)
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

