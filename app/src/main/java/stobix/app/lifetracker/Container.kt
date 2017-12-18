package stobix.app.lifetracker

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity
data class Container(
        @PrimaryKey var containerID: Int,
        var contents: ArrayList<ContainerContent> = ArrayList())

enum class ContainerContentType {
    INT, STRING, PROPERTY
}

@Entity
class ContainerContent(
        @PrimaryKey var id: Int,
        var type: ContainerContentType,
        var typeID: Int,
        var amount: Int?,
        var recur: Container?
)


class ContainerTester{
    val g = Gson()
    var cl:ArrayList<ContainerContent>
    var c:Container
    var c1:Container
    val t = object : TypeToken<ArrayList<ContainerContent>>() {}.type
    val t2 = object : TypeToken<Container>() {}.type
    init{
        cl = ArrayList<ContainerContent>()
        /*
        cl.add(StringContainerEntry(
                0,0,0,"hej")
        )
        */
        cl.add(ContainerContent(
                0,
                ContainerContentType.INT,
                0,
                null,
                null
        ))
        c1 = Container(1)
        cl.add(ContainerContent(
                1,
                ContainerContentType.STRING,
                0,
                3,
                c1

        ))
        c = Container(0, cl)
        /*
        c.contents.add(PropertyContainerEntry(
                0,0,0,"test",
                3,true,"testelitest"
        ))
        */
    }
    fun toJSON() = g.toJson(c,t2)
    fun toJSON(c:Container) = g.toJson(c,t2)
    fun fromJSON(s:String):Container = g.fromJson<Container>(s,t2)
}
/*
 *
 *  [[Int,String],String,Property:Amount "Some thing"]
 *  ->
 *  Container
 *      containerID: CID
 *  ContainerContainerEntry
 *      id: X
 *      pos: 0
 *      containerID: CID
 *  IntContainerEntry
 *      id: _
 *      containerID: X
 *      pos: 0
 *      thing: Int
 *  StringContainerEntry
 *      id: _
 *      containerID: X
 *      pos: 1
 *      thing: String
 *  StringContainerEntry
 *      id: _
 *      containerID: CID
 *      pos: 1
 *      thing: String
 *  PropertyContainerEntry
 *      id: _
 *      containerID: CID
 *      pos: 2
 *      property: Property
 *      amount: Amount
 *      amountable: true
 *      description: "Some thing"
 *
 */
