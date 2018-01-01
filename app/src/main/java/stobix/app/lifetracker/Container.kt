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
        val g = GsonBuilder()
                .registerTypeAdapterFactory(ContainerAdapterFactory())
                .create()
        val type = object : TypeToken<Container>() {}.type
        @JvmStatic
        fun toJSON(thing: Container) = g.toJson(thing,type)
        @JvmStatic
        fun fromJSON(string: String) = g.fromJson<Container>(string,type)
    }

    override operator fun equals(other: Any?) = when(other){
        is Container -> this.containerID == other.containerID && this.contents == other.contents
        else -> false
    }
    override fun hashCode() = super.hashCode()
}



class IntContent(id: Int,val intValue: Int) : ContainerContent(id) {

}

class ContainerAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson?, type: TypeToken<T>?): TypeAdapter<T>? {
        return when(type?.type){
            Container.type -> {
                val cadapter = gson!!.getAdapter(object : TypeToken<ContainerContent>() {})
                (object : TypeAdapter<Container>() {
                    override fun write(out: JsonWriter?, value: Container?) {
                        if (value == null) {
                            out?.nullValue()
                            return
                        }
                        out?.beginArray()
                        out?.value(value.containerID)
                        out?.beginArray()
                        value.contents.forEach {
                            cadapter.write(out,it)
                        }
                        out?.endArray()
                        out?.endArray()
                    }

                    override fun read(reader: JsonReader?): Container {
                        reader!!.beginArray()
                        val c = Container(0)
                        c.containerID=reader.nextInt()
                        reader.beginArray()
                        while (reader.hasNext()) {
                            val v = cadapter.read(reader)
                            c.addChild(v)
                        }
                        reader.endArray()
                        reader.endArray()
                        return c
                    }

                }) as TypeAdapter<T>
            }
            ContainerContent.type -> {
                val cadapter = gson!!.getAdapter(object : TypeToken<Container>() {})
                val adapter = object : TypeAdapter<ContainerContent>() {
                    override fun write(out: JsonWriter?, value: ContainerContent) {
                        out!!.beginArray()
                        out.value(value.type.name)
                        out.value(value.id)
                        out.value(value.amount)
                        cadapter.write(out,value.recur)
                        out.endArray()
                    }

                    override fun read(reader: JsonReader?): ContainerContent {
                        reader!!.beginArray()
                        val ctype = ContainerContentType.valueOf(reader.nextString() ?: "EMPTY")
                        val id = reader.nextInt()
                        val c = ContainerContent(id=id,type=ctype)
                        mebbeh(reader) {
                            c.amount = reader.nextInt()
                        }
                        mebbeh(reader){
                            c.recur = cadapter.read(reader)
                        }
                        reader.endArray()
                        return c
                    }

                    private fun mebbeh(reader: JsonReader,f: () -> Unit){
                        if(reader.peek() == JsonToken.NULL) {
                            reader.nextNull()
                        } else {
                            f()
                        }

                    }

                }
                adapter as TypeAdapter<T>
            }
            else -> {
                null
            }
        }
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
) {
    override operator fun equals(other: Any?): Boolean =
            when(other){
                is ContainerContent ->
                    this.id == other.id
                            && this.type == other.type
                            && this.amount == other.amount
                            && this.recur == other.recur
                else -> false
            }

    override fun hashCode() = super.hashCode()

    companion object {
        val type = object : TypeToken<ContainerContent>() {}.type
    }
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
