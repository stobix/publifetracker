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

        // Increase this each time the JSON generating algorithm gets updated.
        val currentVersion = 6

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
                        out.value(currentVersion)
                        // Write out the SugarEntries using an appropriately short format
                        // For now, it's even JSON compatible!
                        out.beginArray()
                        for(entry in value.entries){
                            out.beginObject()
                            with(entry){
                                out.name("t").value(timestamp)
                                out.name("s").value(sugarLevel)
                                out.name("w").value(weight)
                                out.name("i").value(insulin)
                                out.name("tr").value(treatment)
                                out.name("f").value(food)
                                out.name("d").value(drink)
                                out.name("e").value(extra)
                            }
                            out.endObject()
                        }
                        out.endArray()
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
                        fun splitTreatment(value: String, entry:SugarEntry){
                            // todo: split string between ", "'s to find insulin values
                            try {
                                // assume all lone integers are insulin values
                                entry.insulin=value.toDouble()
                                return
                            } catch (e:NumberFormatException ) {
                                val values = value.split(", ")
                                if (values.size <= 1) {
                                    val maybeADoubleWithComma = value.split(',')
                                    if(maybeADoubleWithComma.size == 2 && maybeADoubleWithComma[0].toIntOrNull() != null && maybeADoubleWithComma[1].toIntOrNull() != null)
                                        splitTreatment("${maybeADoubleWithComma[0].toInt()}.${maybeADoubleWithComma[1].toInt()}",entry)
                                    else {
                                        entry.treatment = value
                                    }
                                    return
                                }
                                // probably an int value
                                var insulin: Double? = null
                                var res = ""
                                for(v in values) {
                                    val maybeInsulin = v.toDoubleOrNull()
                                    if (maybeInsulin == null)
                                        if(res == "")
                                            res = v
                                        else
                                            res += ", ${v}"
                                    else {
                                        if (insulin == null) {
                                            insulin = maybeInsulin
                                        }
                                        else{
                                            // Only one insulin value allowed, abort!
                                            entry.treatment = value
                                            return
                                        }
                                    }
                                }
                                entry.insulin = insulin
                                entry.treatment = res
                                return
                            }
                        }

                        @Suppress("NAME_SHADOWING")
                        val reader = reader ?: error("Null reader")
                        val entries: List<SugarEntry> =
                                unlessNextNull(reader,listOf()){
                                    // If we start with a number, we have a versioned file
                                    if (reader.peek() == JsonToken.NUMBER) {
                                        val version = reader.nextInt()
                                        when (version) {
                                            1 -> readObjectArray(reader){
                                                name,entry ->
                                                when(name) {
                                                    // TODO: Make sure no two items have the same timestamp!
                                                    "uid" -> reader.nextInt()
                                                    "epochTimestamp" -> entry.timestamp = reader.nextLong()
                                                    "sugarLevel" -> entry.sugarLevel = reader.nextInt()
                                                    "weight" -> entry.weight = reader.nextInt()
                                                    "extra" -> entry.extra = reader.nextString()
                                                }
                                            }
                                            2 -> readObjectArray(reader){
                                                name,entry ->
                                                when(name) {
                                                    "epochTimestamp" -> entry.timestamp = reader.nextLong()
                                                    "sugarLevel" -> entry.sugarLevel = reader.nextInt()
                                                    "weight" -> entry.weight = reader.nextInt()
                                                    "extra" -> entry.extra = reader.nextString()
                                                }
                                            }
                                            3 -> readObjectArray(reader) {
                                                name, entry ->
                                                when (name) {
                                                    "epochTimestamp" -> entry.timestamp = reader.nextLong()
                                                    "sugarLevel" -> entry.sugarLevel = reader.nextInt()
                                                    "weight" -> entry.weight = reader.nextInt()
                                                    "treatment" -> splitTreatment(reader.nextString(),entry)
                                                    "food" -> entry.food = reader.nextString()
                                                    "drink" -> entry.drink = reader.nextString()
                                                    "extra" -> entry.extra = reader.nextString()
                                                }
                                            }
                                            4 -> readObjectArray(reader){
                                                name,entry ->
                                                when(name) {
                                                    "timestamp" -> entry.timestamp = reader.nextLong()
                                                    "sugarLevel" -> entry.sugarLevel = reader.nextInt()
                                                    "weight" -> entry.weight = reader.nextInt()
                                                    "treatment" -> splitTreatment(reader.nextString(),entry)
                                                    "food" -> entry.food = reader.nextString()
                                                    "drink" -> entry.drink = reader.nextString()
                                                    "extra" -> entry.extra = reader.nextString()
                                                }
                                            }
                                            5 -> readObjectArray(reader){
                                                name,entry ->
                                                when(name) {
                                                    "t" -> entry.timestamp = reader.nextLong()
                                                    "s" -> entry.sugarLevel = reader.nextInt()
                                                    "w" -> entry.weight = reader.nextInt()
                                                    "tr" -> splitTreatment(reader.nextString(),entry)
                                                    "f" -> entry.food = reader.nextString()
                                                    "d" -> entry.drink = reader.nextString()
                                                    "e" -> entry.extra = reader.nextString()
                                                }
                                            }
                                            6 -> readObjectArray(reader){
                                                name,entry ->
                                                when(name) {
                                                    "t" -> entry.timestamp = reader.nextLong()
                                                    "s" -> entry.sugarLevel = reader.nextInt()
                                                    "w" -> entry.weight = reader.nextInt()
                                                    "i" -> entry.insulin = reader.nextDouble()
                                                    "tr" -> entry.treatment = reader.nextString()
                                                    "f" -> entry.food = reader.nextString()
                                                    "d" -> entry.drink = reader.nextString()
                                                    "e" -> entry.extra = reader.nextString()
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
                                                "epochTimestamp" -> entry.timestamp = reader.nextLong()
                                                "sugarLevel" -> entry.sugarLevel = reader.nextInt()
                                                "extra" -> entry.extra = reader.nextString()
                                            }
                                        }
                                    }
                                }
                        return SugarEntryGsonWrapper(entries)
                    }
                }
                adapter as TypeAdapter<T>
            }

            else -> null
        }
    }
}