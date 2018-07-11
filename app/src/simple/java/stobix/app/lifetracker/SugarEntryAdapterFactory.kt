package stobix.app.lifetracker

import android.util.Log
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class SugarEntryAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson?, type: TypeToken<T>?): TypeAdapter<T>? {
        fun unlessNextNull(reader: JsonReader, f: () -> Unit){
            if(reader.peek() == JsonToken.NULL) {
                reader.nextNull()
            } else {
                f()
            }
        }
        fun<A> unlessNextNull(reader: JsonReader, default:A, f: () -> A) =
            if(reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                default
            } else {
                f()
            }

        var version = 0

        val sugarEntryListTypeToken = object: TypeToken<List<SugarEntry>>() {}

        @Suppress("UNCHECKED_CAST")
        return when(type?.type){
            SugarEntryGsonWrapper.type -> {
                val listAdapter = gson!!.getAdapter(sugarEntryListTypeToken)
                val adapter = object : TypeAdapter<SugarEntryGsonWrapper>() {
                    override fun write(out: JsonWriter?, value: SugarEntryGsonWrapper?) {
                        out ?: return
                        if (value == null) {
                            out.nullValue()
                            return
                        }
                        out.value(value.version)
                        listAdapter.write(out,value.entries)
                    }

                    fun readObjectArray(reader: JsonReader,f: (String,SugarEntry) -> Unit) : MutableList<SugarEntry> {
                        reader.beginArray()
                        val entries = mutableListOf<SugarEntry>()
                        while (reader.hasNext()) {
                            reader.beginObject()
                            val entry = SugarEntry()
                            while (reader.hasNext()) {
                                f (reader.nextName(),entry)
                            }
                            reader.endObject()
                            entries.add(entry)
                        }
                        reader.endArray()
                        return entries
                    }

                    override fun read(reader: JsonReader?): SugarEntryGsonWrapper {
                        @Suppress("NAME_SHADOWING")
                        val reader = reader ?: error("Null reader")
                        val entries: List<SugarEntry> =
                                unlessNextNull(reader,listOf()){
                                    // If we start with a number, we have a versioned file
                                    if (reader.peek() == JsonToken.NUMBER) {
                                        version = reader.nextInt()
                                        when (version) {
                                            1 -> readObjectArray(reader){
                                                name,entry ->
                                                when(name) {
                                                    // TODO: Make sure no two items have the same timestamp!
                                                    "uid" -> reader.nextInt()
                                                    "epochTimestamp" -> entry.epochTimestamp = reader.nextLong()
                                                    "sugarLevel" -> entry.sugarLevel = reader.nextInt()
                                                    "weight" -> entry.weight = reader.nextInt()
                                                    "extra" -> entry.extra = reader.nextString()
                                                }
                                            }
                                            2 -> readObjectArray(reader){
                                                name,entry ->
                                                when(name) {
                                                    "epochTimestamp" -> entry.epochTimestamp = reader.nextLong()
                                                    "sugarLevel" -> entry.sugarLevel = reader.nextInt()
                                                    "weight" -> entry.weight = reader.nextInt()
                                                    "extra" -> entry.extra = reader.nextString()
                                                }
                                            }
                                            3 -> readObjectArray(reader){
                                                name,entry ->
                                                when(name) {
                                                    "epochTimestamp" -> entry.epochTimestamp = reader.nextLong()
                                                    "sugarLevel" -> entry.sugarLevel = reader.nextInt()
                                                    "weight" -> entry.weight = reader.nextInt()
                                                    "treatment" -> entry.treatment = reader.nextString()
                                                    "food" -> entry.food = reader.nextString()
                                                    "drink" -> entry.drink = reader.nextString()
                                                    "extra" -> entry.extra = reader.nextString()
                                                }
                                            }
                                            else -> listAdapter.read(reader)
                                        }
                                    }
                                    // Else, we have a pre-versions file consisting of version 0 SugarEntries
                                    else {
                                        Log.d("file","Unversioned JSON sugar entry file!")
                                        readObjectArray(reader){
                                            name,entry ->
                                            when(name) {
                                                // TODO: Make sure no two items have the same timestamp!
                                                "uid" -> reader.nextInt()
                                                "epochTimestamp" -> entry.epochTimestamp = reader.nextLong()
                                                "sugarLevel" -> entry.sugarLevel = reader.nextInt()
                                                "extra" -> entry.extra = reader.nextString()
                                            }
                                        }
                                    }
                                }
                        return SugarEntryGsonWrapper(version,entries)
                    }
                }
                adapter as TypeAdapter<T>
            }

            else -> null
        }
    }
}