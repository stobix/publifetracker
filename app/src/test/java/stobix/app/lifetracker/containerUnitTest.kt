package stobix.app.lifetracker


import org.junit.Assert.*
import org.junit.Test

class ContainerUnitTest{

    private infix fun <A> A.asEq(a: A) = assertEquals(this,a)
    private infix fun <A> A.asNEq(a: A) = assertNotEquals(this,a)


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
    fun classDispatching(){
        val c = Container()
        c.addInt(0,description="inte ger, tar")
        c.addString("fjorton",14)
        c.addProperty("skolbuss",2,"resa till staden")
        System.out.println("Klasser: ${c.toJSON()}")
        val d = Container.fromJSON(c.toJSON())
        c.toJSON() asEq d.toJSON()
        // TODO get the equals function of c to be recursively valid for equivalent contents
        c asEq d
    }

    @Test
    fun minIntContent(){
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
        c.toJSON() asEq d.toJSON()
        c asEq d
    }

    @Test
    fun fullIntContent(){
        val c = Container()
        c.addInt(0,"int")
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
        c.toJSON() asEq d.toJSON()
        c asEq d
    }

    @Test
    fun minStringContent(){
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
        c.toJSON() asEq d.toJSON()
        c asEq d
    }

    @Test
    fun fullStringContent(){
        val c = Container()
        c.addString("0",3,"string")
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
        c.toJSON() asEq d.toJSON()
        c asEq d
    }

    @Test
    fun minEmptyContainerContent(){
        val c = Container()
        c.addContainer(Container())
        System.out.println(c.toJSON())
        val d = Container.fromJSON(c.toJSON())
        System.out.println(d.toJSON())
        assertTrue(when(c.contents[0]){
            is ContainerContainerContent ->
                true
            else -> false
        } )
        assertTrue(when(d.contents[0]){
            is ContainerContainerContent ->
                true
            else -> false
        } )
        c.toJSON() asEq d.toJSON()
        c asEq d
    }

    @Test
    fun fullEmptyContainerContent(){
        val c = Container()
        c.addContainer(Container(),3,"container")
        System.out.println(c.toJSON())
        val d = Container.fromJSON(c.toJSON())
        System.out.println(d.toJSON())
        assertTrue(when(c.contents[0]){
            is ContainerContainerContent ->
                true
            else -> false
        } )
        assertTrue(when(d.contents[0]){
            is ContainerContainerContent ->
                true
            else -> false
        } )
        c.toJSON() asEq d.toJSON()
        c asEq d
    }

    @Test
    fun intContainerContainer(){
        val c = Container()
        c.addContainer(Container().addInt(0))
        System.out.println(c.toJSON())
        val d = Container.fromJSON(c.toJSON())
        System.out.println(d.toJSON())
        assertTrue(when(c.contents[0]){
            is ContainerContainerContent ->
                true
            else -> false
        } )
        assertTrue(when(d.contents[0]){
            is ContainerContainerContent ->
                true
            else -> false
        } )
        c.toJSON() asEq d.toJSON()
        c asEq d
    }

    @Test
    fun stringContainerContainer(){
        val c = Container()
        c.addContainer(Container().addString("0"))
        System.out.println(c.toJSON())
        val d = Container.fromJSON(c.toJSON())
        System.out.println(d.toJSON())
        assertTrue(when(c.contents[0]){
            is ContainerContainerContent ->
                when((c.contents[0] as ContainerContainerContent).value?.contents?.get(0)){
                    is StringContent -> true
                    else -> false
                }
            else -> false
        } )
        assertTrue(when(d.contents[0]){
            is ContainerContainerContent ->
                when((d.contents[0] as ContainerContainerContent).value?.contents?.get(0)){
                    is StringContent -> true
                    else -> false
                }
            else -> false
        } )
        c.toJSON() asEq d.toJSON()
        c asEq d
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
}