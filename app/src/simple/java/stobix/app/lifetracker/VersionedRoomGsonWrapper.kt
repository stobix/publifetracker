package stobix.app.lifetracker

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import stobix.view.containerview.Content

data class VersionedRoomGsonWrapper<E>(
    var version: Int,
    var entries: List<E>
)

data class SugarEntryGsonWrapper(
        var version: Int,
        var entries: List<SugarEntry>
){

    fun toJSON() = gson.toJson(this,type)

    companion object {
        val gson = GsonBuilder()
                .registerTypeAdapterFactory(SugarEntryAdapterFactory())
                .create()
        @JvmStatic fun fromJSON(string: String) =
                gson.fromJson<SugarEntryGsonWrapper>(string,type)
        @JvmStatic val type = object : TypeToken<Content>() {}.type
    }
}
