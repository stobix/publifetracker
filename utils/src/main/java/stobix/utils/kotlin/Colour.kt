package stobix.utils.kotlin

import android.graphics.Color
import stobix.utils.kotlinExtensions.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

@ExperimentalUnsignedTypes
class Colour(val a:UByte, val r:UByte, val g:UByte, val b:UByte) {

    init{println("a $a r $r g $g b $b")}

    constructor(color:Int):this(
            ((color ushr 6) and 0xFF).toUByte(),
            ((color ushr 4) and 0xFF).toUByte(),
            ((color ushr 2) and 0xFF).toUByte(),
            (color and 0xFF).toUByte())

    constructor(q: Quadruple<UByte,UByte,UByte,UByte>): this(q.first,q.second,q.third,q.fourth)

    override fun toString() = "$a $r $g $b"


    private fun minusTo00i(a:Int, b:Int) = if (a < 0) 0 else a

    private fun <A,B,C> generateBinaryOn(a: A, b: A, f: (B, B) -> C) : (((A)->B)-> C) =
            {ac -> f(ac(a),ac(b))}

    private fun <A,B,C,D> generateBinaryOf(a: A, c: C, f: (B, C) -> D) : (((A)->B)-> D)
            =
            {ac -> f(ac(a),c)}



    private fun <A> createApplyToAll(f:((Colour) -> UByte) -> A) =
            f {it.a} to f {it.r} to f {it.r} to f {it.r}

    private class ColorDimensionPoint(val c1:Colour, val c2:Colour, val steps:UInt, var currentPoint:UInt) {


        // Since pixels are stored as square roots, we need to "unpack" them here and "pack" them again when finished
        private fun unwrapComponent(a:Int) = (a*a).toFloat()
        private fun wrapComponent(f:Float) = sqrt(f).roundToInt().toUByte()
        private fun plus(a:Float,b:Float)=a+b
        private fun minus(a:Float,b:Float)=a-b

        val c1q = c1.toQuad().map(::unwrapComponent)
        val c2q= c2.toQuad().map(::unwrapComponent)

        val step= c2q.zipWith(::minus,c1q).map {it/steps.toFloat()}

        fun currStep(currentPoint: UInt) = step.map { it * currentPoint.toFloat()}

        val color:Colour
            get() = Colour( c1q.zipWith(::plus,currStep(currentPoint)).map(::wrapComponent))
    }

    fun toByteQuad() = this.a to this.r to this.g to this.b
    fun toQuad() = (this.a to this.r to this.g to this.b).map {it.toInt()}


    class ColorRangeIterator(val start:Colour, val endInclusive: Colour, val steps:UInt = 10u): Iterator<Colour> {

        override fun hasNext(): Boolean = dimensionPoint.steps>=dimensionPoint.currentPoint

        private val dimensionPoint = ColorDimensionPoint(start,endInclusive,steps,0u)

        override fun next(): Colour =
                if (hasNext()) {
                    val c = dimensionPoint.color
                    dimensionPoint.currentPoint++
                    c
                }
                else
                    throw IndexOutOfBoundsException("lol")
    }


    class ColorRange(val start:Colour, val endInclusive: Colour, private val steps:UInt = 10u): Iterable<Colour> {

        override fun iterator() = ColorRangeIterator(start, endInclusive, steps)

        infix fun steps(steps: UInt) = ColorRange(start,endInclusive,steps)
        infix fun steps(steps: Int) = steps(steps.toUInt())

    }

    operator fun rangeTo(other:Colour)= ColorRange(this,other)

    // fun toColor(): Int = (a.toInt() shl 6) + (r.toInt() shl 4) + (g.toInt() shl 2) + (b.toInt())

    val color
        get() = Color.argb(a.toInt(),r.toInt(),g.toInt(),b.toInt())

}
