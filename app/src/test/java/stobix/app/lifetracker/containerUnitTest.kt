package stobix.app.lifetracker


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.junit.Assert.*
import org.junit.Test

class ContainerUnitTest{
    val simpleObjStr ="[0,[]]"
    val simpleObj = Container(0)

    private infix fun <A> A.asEq(a: A) = assertEquals(this,a)
    private infix fun <A> A.asNEq(a: A) = assertNotEquals(this,a)

    @Test
    fun serializeSimpleContainer(){

        assertEquals(simpleObjStr,simpleObj.toJSON())
        assertEquals(simpleObjStr,Container.toJSON(simpleObj))
    }

    @Test
    fun deserializeSimpleContainer(){
        val a = Container.fromJSON(simpleObjStr)
        a asEq  simpleObj
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
        baseContainer asEq Container.fromJSON(baseContainer.toJSON())
    }


    @Test
    fun testLargeContainer(){
        val baseContainer = Container(0)
        val recurContainer = Container(1)
        baseContainer.addInt(50,"sugar level")
        baseContainer.addContainer(recurContainer,2,description = "breakfast")
        val recurContainer1 = Container(2)
        recurContainer1.addString("bread")
        recurContainer1.addString("cheese")
        recurContainer1.addString("butter")
        recurContainer.addString("tea")
        recurContainer.addContainer(recurContainer1)
        System.out.println("Large container: ${baseContainer.toJSON()}")

        baseContainer asEq  Container.fromJSON(baseContainer.toJSON())
    }

    @Test
    fun testEquals(){
        val c1 = Container(0)
        val c2 = Container(0)
        val c3 = Container(0)
        val c4 = Container(0)
        c1.addInt(0)
        c2.addInt(0)
        c4.addContainer(c1)
        c1 asEq c1
        c1.contents  asEq  c2.contents
        c1 asEq c2
        c1 asNEq c3
        c1 asNEq c4
    }

    @Test
    fun factory(){
        val g = GsonBuilder().registerTypeAdapterFactory(ContainerAdapterFactory()).create()
        val c = Container()
        val cjson = c.toJSON()
        val cc = ContainerContent(0,ContainerContentType.CONTAINER)
        var ccjson = Container.g.toJson(cc)
        System.out.println("container: $cjson")
        Container() asEq g.fromJson(cjson,Container.type)
        cc asEq g.fromJson(ccjson,ContainerContent.type)
        System.out.println("contents: $ccjson")
        cc.recur = c
        ccjson=g.toJson(cc)
        System.out.println("recur contents: $ccjson")
        val cc1 = g.fromJson<ContainerContent>(ccjson,ContainerContent.type)
        System.out.println("cc1 recur is ${cc1.recur}")
        val cc1json=g.toJson((cc1))
        System.out.println("from contents: $cc1json")
        cc asEq cc1
    }

}