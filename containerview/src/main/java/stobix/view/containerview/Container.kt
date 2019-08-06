package stobix.view.containerview

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import stobix.view.containerview.ContentType.*

@Entity
data class Container(
        @PrimaryKey(autoGenerate = true) var containerID: Int?=null,
        var contents: ArrayList<Content> = ArrayList()) {

    fun toJSON() = g.toJson(this, type)

    fun addChild(c: Content) : Container {
        this.contents.add(c)
        return this
    }

    fun addContainer(c: Container, amount: Int?=null, description: String?=null) =
            addChild(ContainerContent(value = c, amount = amount, description = description))

    fun addInt(i: Int, description: String?=null) =
            addChild(IntContent(value = i, description = description))

    fun addString(s: String,amount: Int?=null,description: String?=null) =
            addChild(StringContent(value = s, amount = amount, description = description))

    // FIXME Is there a point to having this?
    fun addProperty(s: String, amount: Int?=null,description: String?=null){
        val c = Container()
        c.addString(s)
        c.addInt(-1,description=description)
        this.addContainer(c,amount)
    }

    companion object {
        val g = GsonBuilder()
                .registerTypeAdapterFactory(ContainerAdapterFactory())
                .create()
        val type = object : TypeToken<Container>() {}.type
        @JvmStatic
        fun toJSON(thing: Container) = g.toJson(thing, type)
        @JvmStatic
        fun fromJSON(string: String) = g.fromJson<Container>(string, type)
    }

    override operator fun equals(other: Any?) = when(other){
        is Container -> {
            if (this.contents.size == other.contents.size) {
                var g = true
                for ((i, v) in this.contents.withIndex()) {
                    g = g && v == other.contents[i]
                }
                g && this.containerID == other.containerID
            } else false
        }
        else -> false
    }
    override fun hashCode() = super.hashCode()
}

open class Content( var type: ContentType ) {

    override fun hashCode() = super.hashCode()

    override fun equals(other: Any?): Boolean =
            when(other){
                is Content -> {
                    System.out.println("got called")
                            this.type == other.type
                }
                else -> false
            }

    companion object {
        @JvmStatic val type = object : TypeToken<Content>() {}.type
    }
}

@Entity(tableName="int_content")
data class IntContent @JvmOverloads constructor(
        @PrimaryKey(autoGenerate = true) var id: Int?=null,
        var value: Int?=null,
        var description: String?=null
)
    :
        Content( INT ) {

    override fun equals(other: Any?): Boolean =
            when(other){
                is IntContent ->
                    this.id == other.id
                            && this.type == other.type
                            && this.description == other.description
                            && this.value == other.value
                else -> false
            }

    override fun hashCode() = super.hashCode()

}

@Suppress("EqualsOrHashCode")
@Entity(tableName="string_content")
data class StringContent(
        @PrimaryKey(autoGenerate = true) var id: Int?=null,
        var value: String?=null,
        var amount: Int?=null,
        var description: String?=null
        )
    :
        Content( STRING ) {
    override fun equals(other: Any?): Boolean =
            when(other){
                is StringContent ->
                    this.id == other.id
                            && this.type == other.type
                            && this.amount == other.amount
                            && this.description == other.description
                            && this.value == other.value
                else -> false
            }

}


data class ContainerContent(
        @PrimaryKey(autoGenerate = true) var id: Int?=null,
        var value: Container?=null,
        var amount: Int?=null,
        var description: String?=null
)
    :
        Content( CONTAINER ) {

    override fun equals(other: Any?): Boolean =
            when(other){
                is ContainerContent ->
                    this.id == other.id
                            && this.type == other.type
                            && this.amount == other.amount
                            && this.description == other.description
                            && this.value == other.value
                else -> false
            }

    override fun hashCode() = super.hashCode()
}
@Suppress("UNCHECKED_CAST")
class ContainerAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson?, type: TypeToken<T>?): TypeAdapter<T>? {
        fun unlessNextNull(reader: JsonReader,f: () -> Unit){
            if(reader.peek() == JsonToken.NULL) {
                reader.nextNull()
            } else {
                f()
            }

        }
        return when(type?.type){
            Container.type -> {
                val contentAdapter = gson!!.getAdapter(object : TypeToken<Content>() {})
                val adapter = (object : TypeAdapter<Container>() {
                    override fun write(out: JsonWriter?, value: Container?) {
                        if (value == null) {
                            out?.nullValue()
                            return
                        }
                        out?.beginArray()
                        out?.value(value.containerID)
                        out?.beginArray()
                        value.contents.forEach {
                            contentAdapter.write(out,it)
                        }
                        out?.endArray()
                        out?.endArray()
                    }

                    override fun read(reader: JsonReader?): Container {
                        reader!!.beginArray()
                        val c = Container()
                        unlessNextNull(reader) {
                            c.containerID = reader.nextInt()
                        }
                        reader.beginArray()
                        while (reader.hasNext()) {
                            val v: Content = contentAdapter.read(reader)
                            c.addChild(v)
                        }
                        reader.endArray()
                        reader.endArray()
                        return c
                    }

                })
                adapter as TypeAdapter<T>
            }
            Content.type -> {
                val containerAdapter = gson!!.getAdapter(object : TypeToken<Container>() {})
                val adapter = object : TypeAdapter<Content>() {
                    override fun write(out: JsonWriter?, value: Content) {
                        out!!.beginArray()
                        out.value(value.type.name)

                        when(value.type){
                            INT -> {
                                val i = value as IntContent
                                out.value(i.id)
                                out.value(i.value)
                                out.value(i.description)
                            }
                            CONTAINER -> {
                                val c = value as ContainerContent
                                out.value(c.id)
                                out.value(c.amount)
                                out.value(c.description)
                                containerAdapter.write(out, c.value)
                            }
                            STRING -> {
                                val s = value as StringContent
                                out.value(s.id)
                                out.value(s.amount)
                                out.value(s.value)
                                out.value(value.description)
                            }
                            else -> error("unhandled content type")

                        }
                        out.endArray()
                    }

                    override fun read(reader: JsonReader?): Content {
                        reader!!.beginArray()
                        @Suppress("RemoveRedundantQualifierName")
                        val ctype = ContentType.valueOf(reader.nextString())

                        val ret: Content = when (ctype) {
                            CONTAINER -> {
                                val c = ContainerContent()
                                arrayOf(
                                        { c.id = reader.nextInt()},
                                        { c.amount = reader.nextInt() },
                                        { c.description = reader.nextString() },
                                        { c.value = containerAdapter.read(reader) }
                                ).map {unlessNextNull(reader,it)}
                                c
                            }
                            INT -> {
                                val c = IntContent()
                                arrayOf(
                                        { c.id = reader.nextInt()},
                                        { c.value = reader.nextInt() },
                                        { c.description = reader.nextString() }
                                ).map {unlessNextNull(reader,it)}
                                c
                            }
                            STRING -> {
                                val c = StringContent()
                                arrayOf(
                                        { c.id = reader.nextInt()},
                                        { c.amount = reader.nextInt() },
                                        { c.value = reader.nextString() },
                                        { c.description = reader.nextString() }
                                ).map {unlessNextNull(reader,it)}
                                c
                            }
                            else -> error("unhandled content type")
                        }
                        reader.endArray()
                        return ret
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

enum class ContentType {
    INT, STRING, PROPERTY, CONTAINER
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
