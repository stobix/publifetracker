package stobix.app.lifetracker


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Test
import org.junit.Assert.assertEquals
import stobix.app.lifetracker.Container.Companion.type

class ContainerUnitTest{
    val simpleObjStr ="""{"containerID":0,"contents":[]}"""
    val simpleObj = Container(0)

    @Test
    fun serializeSimpleContainer(){
        val type = object : TypeToken<Container>() {}.type
        fun toJSON() = Gson().toJson(simpleObj,type)

        assertEquals(simpleObjStr,toJSON())
        assertEquals(simpleObjStr,simpleObj.toJSON())
        assertEquals(simpleObjStr,Container.toJSON(simpleObj))
        assertEquals(simpleObjStr,Container._toJSON(simpleObj))
    }

    @Test
    fun deserializeSimpleContainer(){
        val a = Container.fromJSON(simpleObjStr)
        assert(a == simpleObj)
    }

    @Test
    fun testRecursiveContainer(){
        val baseContainer = Container(0)
        val recurContainer = Container(1)
        val child = ContainerContent(
                0,
                recur=recurContainer
        )
        baseContainer.contents.add(child)
        assert(baseContainer == Container.fromJSON(baseContainer.toJSON()))
    }


    @Test
    fun testLargeContainer(){
        val baseContainer = Container(0)
        val recurContainer = Container(1)
        baseContainer.addChild(type=ContainerContentType.INT,typeID=0,amount=1)
        baseContainer.addChild(type=ContainerContentType.STRING)
        baseContainer.addChild(type=ContainerContentType.PROPERTY)
        baseContainer.addChild(type=ContainerContentType.CONTAINER,recur=recurContainer)
        val recurContainer1 = Container(2)
        recurContainer1.addChild(type=ContainerContentType.INT)
        recurContainer.addChild(type=ContainerContentType.CONTAINER,recur=recurContainer1)

        assert(baseContainer == Container.fromJSON(baseContainer.toJSON()))
    }

    @Test
    fun testEquals(){
        val c1 = Container(0)
        val c2 = Container(0)
        val c3 = Container(0)
        val c4 = Container(0)
        c1.addChild(0,ContainerContentType.INT,0,0,null)
        c2.addChild(0,ContainerContentType.INT,0,0,null)
        c4.addChild(0,ContainerContentType.INT,0,0,c1)
        assert(c1==c1)
        assert(c1.contents == c2.contents)
        assert(c1==c2)
        assert(c1!=c3)
        assert(c1!=c4)
    }

}