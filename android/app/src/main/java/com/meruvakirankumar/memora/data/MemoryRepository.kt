package com.meruvakirankumar.memora.data

import android.content.Context
import com.meruvakirankumar.memora.model.Confidence
import com.meruvakirankumar.memora.model.EventType
import com.meruvakirankumar.memora.model.Memory
import org.json.JSONArray
import org.json.JSONObject

/** Stores confirmed memories locally as JSON in SharedPreferences. */
class MemoryRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("memora", Context.MODE_PRIVATE)

    fun load(): List<Memory> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        val array = JSONArray(raw)
        return (0 until array.length()).map { fromJson(array.getJSONObject(it)) }
    }

    fun save(memories: List<Memory>) {
        val array = JSONArray()
        memories.forEach { array.put(toJson(it)) }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    private fun toJson(memory: Memory): JSONObject = JSONObject().apply {
        put("id", memory.id)
        put("title", memory.title)
        put("eventType", memory.eventType.name)
        put("dateIso", memory.dateIso)
        put("confidence", memory.confidence.name)
        put("sourceText", memory.sourceText)
        put("createdAtIso", memory.createdAtIso)
        put("completedAtIso", memory.completedAtIso ?: JSONObject.NULL)
        put("notificationId", memory.notificationId ?: JSONObject.NULL)
    }

    private fun fromJson(json: JSONObject): Memory = Memory(
        id = json.getString("id"),
        title = json.getString("title"),
        eventType = EventType.valueOf(json.getString("eventType")),
        dateIso = json.getString("dateIso"),
        confidence = Confidence.valueOf(json.getString("confidence")),
        sourceText = json.getString("sourceText"),
        createdAtIso = json.getString("createdAtIso"),
        completedAtIso = if (json.isNull("completedAtIso")) null else json.getString("completedAtIso"),
        notificationId = if (json.isNull("notificationId")) null else json.getInt("notificationId"),
    )

    private companion object {
        const val KEY = "memories.v1"
    }
}
