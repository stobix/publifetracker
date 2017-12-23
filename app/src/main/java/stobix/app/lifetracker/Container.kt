package stobix.app.lifetracker

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Type

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
        val g_alt = GsonBuilder()
                .registerTypeAdapter(
                        object : TypeToken<ContainerContent>() {}.type,
                        ContainerContentAdapter().nullSafe())
                .create()
        val g = Gson()
        val type = object : TypeToken<Container>() {}.type
        @JvmStatic
        fun toJSON(thing: Container) = g.toJson(thing,type)
        @JvmStatic
        fun fromJSON(string: String) = g.fromJson<Container>(string,type)
        @JvmStatic
        fun _toJSON(thing: Container) = g_alt.toJson(thing,type)
        @JvmStatic
        fun _fromJSON(string: String) = g_alt.fromJson<Container>(string,type)
    }

}

class ContainerContentDeserializer : JsonDeserializer<ContainerContent> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
    ): ContainerContent {
        return IntContent(0,0)
    }
}

class IntContent(id: Int,val intValue: Int) : ContainerContent(id) {

}
// TODO Make an adapterFactory instead, for recursion.
// http://www.javadoc.io/doc/com.google.code.gson/gson/2.8.2
class ContainerContentAdapter : TypeAdapter<ContainerContent>() {

    override fun read(reader: JsonReader): ContainerContent {
        reader.nextNull()
        return ContainerContent(0)
    }

    override fun write(writer: JsonWriter, value: ContainerContent) {
        writer.beginObject()
        writer.name("type")
        writer.value("${value.type}")
        writer.name("value")
        when(value.type) {
            ContainerContentType.CONTAINER ->{
                writer.value(value.recur!!.toJSON())
            }
            ContainerContentType.PROPERTY -> {
                writer.value("property")
            }
            ContainerContentType.STRING ->
                writer.value("string")
            ContainerContentType.INT ->
                writer.value(3)
            ContainerContentType.EMPTY ->
                writer.nullValue()
        }
        if(value.amount!=null) {
            writer.name("amount")
            writer.value(value.amount)
        }
        writer.endObject()
    }

}

enum class ContainerContentType {
    INT, STRING, PROPERTY, CONTAINER,EMPTY
}

@Entity
open class ContainerContent(
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
