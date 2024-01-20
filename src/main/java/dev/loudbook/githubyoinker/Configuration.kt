package dev.loudbook.githubyoinker

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

class Configuration {
    private var configObject: JsonObject? = null

    init {
        loadConfig()
    }

    private fun loadConfig() {
        val gson = Gson()

        try {
            val file = File("./yoinkerconfig.json")

            if (!file.exists()) {
                Main::class.java.getResourceAsStream("/yoinkerconfig.json")
                    .let {
                        if (it == null) {
                            Logger.error("Failed to load the default config. I'm broken. I need help.")
                            exitProcess(0)
                        }

                        Files.copy(it, Path.of("./yoinkerconfig.json"))
                    }

                Logger.log("Copied default configuration file.")
                exitProcess(0)
            }

            configObject = gson.fromJson(FileReader(File("./yoinkerconfig.json")), JsonObject::class.java)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun getValue(key: String): JsonElement {
        val keys = key.split("\\.".toRegex())
        var current = configObject

        for (i in 0 until keys.size - 1) {
            try {
                current = current!![keys[i]].asJsonObject
            } catch (ignored: NullPointerException) {
                throw NullPointerException("Failed to get config value $key")
            }
        }

        if (current == null || current[keys[keys.size - 1]] == null) {
            throw java.lang.NullPointerException("Failed to retrieve config value $key")
        }

        return current[keys[keys.size - 1]]
    }

    val filesArray: JsonArray
        get() = configObject!!.getAsJsonArray("files")
}
