package stobix.app.lifetracker

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity
data class Container(
        @PrimaryKey var containerID: Int,
        var contents: ArrayList<ContainerContent> = ArrayList()) {

    fun toJSON() = g.toJson(this,type)

    fun addChild(c:ContainerContent) = this.contents.add(c)
    fun addChild(
            id: Int=contents.size+1,
            type: ContainerContentType=ContainerContentType.CONTAINER,
            typeID: Int?=null,
            amount: Int?=null,
            recur: Container?=null) =
            addChild(ContainerContent(id,type,typeID,amount,recur))

    companion object {
        val g = Gson()
        val type = object : TypeToken<Container>() {}.type
        @JvmStatic
        fun toJSON(thing: Container) = g.toJson(thing,type)
        @JvmStatic
        fun fromJSON(string: String) = g.fromJson<Container>(string,type)
    }

}

enum class ContainerContentType {
    INT, STRING, PROPERTY, CONTAINER
}

@Entity
class ContainerContent(
        @PrimaryKey var id: Int,
        var type: ContainerContentType=ContainerContentType.CONTAINER,
        var typeID: Int?=null,
        var amount: Int?=null,
        var recur: Container?=null
)


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
