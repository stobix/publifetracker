package stobix.app.lifetracker


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
        val child = ContainerContainerContent(
                id=0,
                value =recurContainer
        )
        baseContainer.addContainer(recurContainer)
        System.out.println("${baseContainer.toJSON()} == ${Container.fromJSON(baseContainer.toJSON()).toJSON()}")
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

    /*
    @Test
    fun factory(){
        val g = GsonBuilder().registerTypeAdapterFactory(ContainerAdapterFactory()).create()
        val c0 = Container()
        val cc: ContainerContent = ContainerContainerContent(0,Container())
        c0.addChild(cc)
        val cjson = c0.toJSON()
        // FIXME This won`t work directly for ContainerContent sub classes. Is this a problem?
        //var ccjson = Container.g.toJson(cc)
        System.out.println("container: $cjson")
        c0 asEq g.fromJson(cjson,Container.type)
        //System.out.println("contents: $ccjson")
        //cc asEq g.fromJson(ccjson,ContainerContent.type)
        (cc as ContainerContainerContent).value = Container()
        //var ccjson=g.toJson(cc)
        //System.out.println("value contents: $ccjson")
        //val cc1 = g.fromJson<ContainerContainerContent>(ccjson,ContainerContent.type)
        //System.out.println("cc1 value is ${cc1.value}")
        //val cc1json=g.toJson((cc1))
        //System.out.println("from contents: $cc1json")
        //cc asEq cc1
    }
    */

    @Test
    fun klasskamp(){
        val c = Container()
        c.addInt(0,description="inte ger, tar")
        c.addString("fjorton",14)
        c.addProperty("skolbuss",2,"resa till staden")
        System.out.println("Klasser: ${c.toJSON()}")
        c.toJSON() asEq Container.fromJSON(c.toJSON()).toJSON()
        // TODO get the equals function of c to be recursively valid for equivalent contents
    }

    @Test
    fun intContent(){
        val c = Container()
        c.addInt(0)
        System.out.println(c.toJSON())
        val d = Container.fromJSON(c.toJSON())
        System.out.println(d.toJSON())
        assertTrue(when(c.contents[0]){
            is IntContent ->
                true
            else -> false
        } )
        assertTrue(when(d.contents[0]){
            is IntContent ->
                true
            else -> false
        } )
        c asEq d
    }

    @Test
    fun stringContent(){
        val c = Container()
        c.addString("0")
        System.out.println(c.toJSON())
        val d = Container.fromJSON(c.toJSON())
        System.out.println(d.toJSON())
        assertTrue(when(c.contents[0]){
            is StringContent ->
                true
            else -> false
        } )
        assertTrue(when(d.contents[0]){
            is StringContent ->
                true
            else -> false
        } )
        c asEq d
    }


}