package stobix.app.lifetracker

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

// Something made the type inspection system choke on a generic, IIRC. This is thus not used yet.
data class VersionedRoomGsonWrapper<E>(
    var version: Int,
    var entries: List<E>
)

/*
 * Used as an outer layer to wrap a list of SugarEntries so that the generated json file
 * contains a version number used to convert old SugarEntry versions to the current one
 * upon read.
 */
data class SugarEntryGsonWrapper(
        var entries: List<SugarEntry>
){

    fun toJSON() = gson.toJson(this,type)

    companion object {
        val gson = GsonBuilder()
                .registerTypeAdapterFactory(SugarEntryAdapterFactory())
                .create()
        @JvmStatic fun fromJSON(string: String) =
                gson.fromJson<SugarEntryGsonWrapper>(string,type)
        @JvmStatic val type = object : TypeToken<SugarEntryGsonWrapper>() {}.type
    }
}
