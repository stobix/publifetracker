package stobix.app.lifetracker


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Test
import org.junit.Assert.assertEquals

class ContainerUnitTest{
    val simpleObjStr ="{\"containerID\":0,\"contents\":[]}"

    @Test
    fun serializeSimpleContainer(){
        val a = Container(0)
        val type = object : TypeToken<Container>() {}.type
        fun toJSON() = Gson().toJson(a,type)

        assertEquals(simpleObjStr,toJSON())
        assertEquals(simpleObjStr,a.toJSON())
        assertEquals(simpleObjStr,Container.toJSON(a))
    }

    @Test
    fun serializeSimpleCustomContainer(){
        val a = Container(0)
        assertEquals(simpleObjStr,Container._toJSON(a))
    }

    /*
    @Test
    fun serializeContainingCustomContainer(){
        val a = Container(0)
        a.addChild(type=ContainerContentType.PROPERTY)
        assertEquals(simpleObjStr,Container._toJSON(a))
    }

    @Test
    fun serializeContainerContainingCustomContainer(){
        val a = Container(0)
        val b = Container(1)
        a.addChild(type=ContainerContentType.CONTAINER,recur=b)
        assertEquals(simpleObjStr,Container._toJSON(a))
    }
    */

    @Test
    fun deserializeSimpleContainer(){
        val a = Container.fromJSON(simpleObjStr)
        assertEquals(0,a.contents.size)
    }

    val recurObjStr =
            """{"containerID":0,"contents":[{"id":0,"type":"CONTAINER","recur":{"containerID":1,"contents":[]}}]}"""
    @Test
    fun serializeRecursiveContainer(){
        val baseContainer = Container(0)
        val recurContainer = Container(1)
        val child = ContainerContent(
                0,
                recur=recurContainer
        )
        baseContainer.contents.add(child)
        assertEquals(recurObjStr,baseContainer.toJSON())
    }

    @Test
    fun deserializeRecursiveContainer(){
       val baseContainer = Container.fromJSON(recurObjStr)
        assertEquals(1,baseContainer.contents[0].recur?.containerID)
    }


    val largeContainerStr = """
        {"containerID":0,"contents":[{"id":1,"type":"INT","typeID":0,"amount":1},{"id":2,"type":"STRING"},{"id":3,"type":"PROPERTY"},{"id":4,"type":"CONTAINER","recur":{"containerID":1,"contents":[]}}]}
        """.trimIndent()
    @Test
    fun testLargeContainer(){
        val baseContainer = Container(0)
        val recurContainer = Container(1)
        baseContainer.addChild(type=ContainerContentType.INT,typeID=0,amount=1)
        baseContainer.addChild(type=ContainerContentType.STRING)
        baseContainer.addChild(type=ContainerContentType.PROPERTY)
        baseContainer.addChild(type=ContainerContentType.CONTAINER,recur=recurContainer)
        assertEquals(largeContainerStr,baseContainer.toJSON())
    }

}