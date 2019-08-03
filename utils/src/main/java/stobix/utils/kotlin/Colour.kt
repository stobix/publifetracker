package stobix.utils.kotlin

import android.graphics.Color
import stobix.utils.kotlinExtensions.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

typealias  FloatQuad = Quadruple<Float,Float,Float,Float>

class Colour(val a:Float, val r:Float, val g:Float, val b:Float) {

    constructor(color:Int):this( Color.alpha(color).toFloat(), Color.red(color).toFloat(), Color.green(color).toFloat(), Color.blue(color).toFloat())
    constructor(q:Quadruple<Float,Float,Float,Float>):this(q.first,q.second,q.third,q.fourth)


    val ra = a*a
    val rr = r*r
    val rg = g*g
    val rb = b*b

    init{
        println("a $a r $r g $g b $b -> $ra $rr $rg $rb")
    }

    override fun toString() = "$a $r $g $b"

    /**
     * Maps a function over a quadruple of the real colour values.
     * @return A color with the new values
     */
    fun quadMap(f: (Quadruple<Float,Float,Float,Float>) -> Quadruple<Float,Float,Float,Float>) =
            Colour(f(ra to rr to rg to rb).map {sqrt(it)})

    fun toRealQuad() = ra to rr to rg to rb


    operator fun rangeTo(other:Colour)= ColorRange(this,other)

    val color
        get() = Color.argb(a.roundToInt(),r.roundToInt(),g.roundToInt(),b.roundToInt())

    companion object {
        fun fromRealQuad(q:FloatQuad) = Colour(q.map{sqrt(it)})
    }
}

class ColorRange(val start:Colour, val endInclusive: Colour, private val steps:Int = 10): Iterable<Colour> {

    override fun iterator() = ColorRangeIterator(start, endInclusive, steps)

    infix fun steps(steps: Int) = ColorRange(start,endInclusive,steps)
}

class ColorRangeIterator(val start:Colour, val endInclusive: Colour, val steps:Int = 10): Iterator<Colour> {

    override fun hasNext(): Boolean = dimensionPoint.steps>=dimensionPoint.currentPoint

    private val dimensionPoint = ColorDimensionPoint(start,endInclusive,steps,0)

    override fun next(): Colour =
            if (hasNext()) {
                val c = dimensionPoint.color
                dimensionPoint.currentPoint++
                c
            }
            else
                throw IndexOutOfBoundsException("lol")
}

class ColorDimensionPoint(val c1:Colour, val c2:Colour, val steps:Int, var currentPoint:Int) {

    // Since pixels are stored as square roots, we need to "unpack" them here and "pack" them again when finished
    private fun plus(a:Float,b:Float)=a+b
    private fun minus(a:Float,b:Float)=a-b

    val step = c2.toRealQuad().zipWith(c1.toRealQuad()){c2c, c1c ->
        (c2c-c1c)/steps.toFloat()
    }

    fun currStep(currentPoint: Int):FloatQuad = step.map { it * currentPoint.toFloat()}

    val color:Colour
        get() = Colour.fromRealQuad(c1.toRealQuad().zipWith(currStep(currentPoint),::plus))
}
