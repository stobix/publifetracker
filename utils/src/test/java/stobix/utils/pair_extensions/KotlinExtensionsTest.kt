package stobix.utils.pair_extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

private infix fun <A> A.asEq(a: A) = assertEquals(this,a)
private infix fun <A> A.asNEq(a: A) = assertNotEquals(this,a)

public class KotlinExtensionsTest {
    @Test
    fun tupleTotripleTo(){
        1 to 2 to 3 asEq Triple(1,2,3)
    }

    @Test
    fun tripleToQuadruple(){
        1 to 2 to 3 to 4 asEq Quadruple(1,2,3,4)
    }
}