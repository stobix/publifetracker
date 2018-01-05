package stobix.app.lifetracker

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

@Entity
data class Container(
        @PrimaryKey(autoGenerate = true) var containerID: Int?=null,
        var contents: ArrayList<ContainerContent> = ArrayList()) {

    fun toJSON() = g.toJson(this,type)

    fun addChild(c:ContainerContent) : Container {
        this.contents.add(c)
        return this
    }

    fun addContainer(c: Container,amount: Int?=null,description: String?=null) =
            addChild(ContainerContainerContent(value=c,amount=amount,description = description))

    fun addInt(i: Int, description: String?=null) =
            addChild(IntContent(value=i,description = description))

    fun addString(s: String,amount: Int?=null,description: String?=null) =
            addChild(StringContent(value=s,amount = amount,description = description))

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
        fun toJSON(thing: Container) = g.toJson(thing,type)
        @JvmStatic
        fun fromJSON(string: String) = g.fromJson<Container>(string,type)
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

@Entity
open class ContainerContent(
        @PrimaryKey var id: Int?=null,
        var type: ContainerContentType=ContainerContentType.PROPERTY,
        var description: String?=null
) {

    override fun hashCode() = super.hashCode()

    override fun equals(other: Any?): Boolean =
            when(other){
                is ContainerContent -> {
                    System.out.println("got called")
                    this.id == other.id
                            && this.type == other.type
                            && this.description == other.description
                }
                else -> false
            }

    companion object {
        val type = object : TypeToken<ContainerContent>() {}.type
    }
}


class IntContent(
        var value: Int?=null,
        description: String?=null,
        id: Int?=null)
    :
        ContainerContent(
                id=id,
                description=description
        ) {
    init{
        this.type=ContainerContentType.INT
    }

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

    companion object {
        val type = object : TypeToken<ContainerContent>() {}.type
    }
}

class StringContent(
        var value: String?=null,
        var amount: Int?=null,
        description: String?=null,
        id: Int?=null)
    :
        ContainerContent(
                id=id,
                description=description
        ) {
    init{
        this.type=ContainerContentType.STRING
    }
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

class ContainerContainerContent(
        var value: Container?=null,
        var amount: Int?=null,
        description: String?=null,
        id: Int?=null)
    :
        ContainerContent(
                id=id,
                description=description
        ) {
    init{
        this.type=ContainerContentType.CONTAINER
    }

    override fun equals(other: Any?): Boolean =
            when(other){
                is ContainerContainerContent ->
                    this.id == other.id
                            && this.type == other.type
                            && this.amount == other.amount
                            && this.description == other.description
                            && this.value == other.value
                else -> false
            }

    override fun hashCode() = super.hashCode()
}
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
                val cadapter = gson!!.getAdapter(object : TypeToken<ContainerContent>() {})
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
                            cadapter.write(out,it)
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
                            val v: ContainerContent = cadapter.read(reader)
                            c.addChild(v)
                        }
                        reader.endArray()
                        reader.endArray()
                        return c
                    }

                })
                adapter as TypeAdapter<T>
            }
            ContainerContent.type -> {
                val cadapter = gson!!.getAdapter(object : TypeToken<Container>() {})
                val adapter = object : TypeAdapter<ContainerContent>() {
                    override fun write(out: JsonWriter?, value: ContainerContent) {
                        out!!.beginArray()
                        out.value(value.type.name)
                        out.value(value.id)

                        when(value.type){
                            ContainerContentType.INT -> {
                                val i = value as IntContent
                                out.value(i.value)
                                out.value(i.description)
                            }
                            ContainerContentType.CONTAINER -> {
                                val c = value as ContainerContainerContent
                                out.value(c.amount)
                                out.value(c.description)
                                cadapter.write(out, c.value)
                            }
                            ContainerContentType.STRING -> {
                                val s = value as StringContent
                                out.value(s.value)
                                out.value(value.description)
                            }
                            else -> error("unhandled content type")

                        }
                        out.endArray()
                    }

                    override fun read(reader: JsonReader?): ContainerContent {
                        reader!!.beginArray()
                        val ctype = ContainerContentType.valueOf(reader.nextString())
                        var id :Int? = null
                        unlessNextNull(reader){id = reader.nextInt()}

                        val ret: ContainerContent = when (ctype) {
                            ContainerContentType.CONTAINER -> {
                                val c = ContainerContainerContent(id=id)
                                arrayOf(
                                        { c.amount = reader.nextInt() },
                                        { c.description = reader.nextString() }
                                ).map {unlessNextNull(reader,it)}
                                unlessNextNull(reader) {
                                    c.value = cadapter.read(reader)
                                }
                                c
                            }
                            ContainerContentType.INT -> {
                                val c = IntContent(id=id)
                                arrayOf(
                                        { c.value = reader.nextInt() },
                                        { c.description = reader.nextString() }
                                ).map {unlessNextNull(reader,it)}
                                c
                            }
                            ContainerContentType.STRING -> {
                                val c = StringContent(id=id)
                                arrayOf(
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

enum class ContainerContentType {
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
