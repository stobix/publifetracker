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

                    override fun read(reader: JsonReader?): SugarEntryGsonWrapper {
                        @Suppress("NAME_SHADOWING")
                        val reader = reader ?: error("Null reader")
                        unlessNextNull(reader){
                            try {
                                version = reader.nextInt()
                            } catch (e: IllegalStateException) {
                               Log.d("file","Unversioned JSON sugar entry file!")
                            }
                        }
                        val entries = listAdapter.read(reader)
                        return SugarEntryGsonWrapper(version,entries)
                    }
                }
                adapter as TypeAdapter<T>
            }

            /*
            // TODO use this or something like this to create a shorter version of the SugarEntry list, using Gson's default parser iff version == 0

            sugarEntryListTypeToken.type -> {
                val adapter = object : TypeAdapter<List<SugarEntry>>() {
                    override fun write(out: JsonWriter?, value: List<SugarEntry>?) {
                    }

                    override fun read(reader: JsonReader?): List<SugarEntry> {
                    }

                }
                adapter as TypeAdapter<T>
            }
            */


            else -> null
        }
    }
}