package dev.loudbook.githubyoinker

import java.io.*

class Cache {
    private var versionMap = mutableMapOf<String, String>()

    init {
        loadCache()
    }

    fun insertVersion(repo: String, version: String) {
        this.versionMap[repo] = version
    }

    fun checkVersion(repo: String): String {
        val currentVersion = versionMap[repo] ?: ""
        return currentVersion
    }

    fun saveCache() {
        val file = getFile()

        val outputStream = ObjectOutputStream(FileOutputStream(file))
        outputStream.writeObject(versionMap)

        outputStream.close()
    }

    private fun loadCache() {
        val file = getFile()

        try {
            val inputStream = ObjectInputStream(FileInputStream(file))

            @Suppress("UNCHECKED_CAST")
            this.versionMap = inputStream.readObject() as MutableMap<String, String>
        } catch (e: EOFException) {
            Logger.warning("Cache was not loaded due to errors.")
        }
    }

    private fun getFile(): File {
        val file = File("./downloadcache")
        if (!file.exists()) {
            file.createNewFile()
        }

        return file
    }
}