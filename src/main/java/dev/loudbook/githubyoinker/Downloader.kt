package dev.loudbook.githubyoinker

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class Downloader(private val configuration: Configuration, private val githubPath: String, private val main: GithubYoinker, private val cache: Cache) {
    private val client: HttpClient = HttpClient.newHttpClient()
    private val destination = configuration.getValue("destinationDirectory").asString

    init {
        initiateDownload()
    }

    private fun initiateDownload() {
        val element = configuration.filesArray.asList().stream()
            .filter { jsonElement: JsonElement -> jsonElement.asJsonObject["repo"].asString == githubPath }.findFirst()
            .orElse(null)

        Logger.debug("Initiating pre download for $githubPath. Running on thread ${Thread.currentThread().name}")

        if (element == null) {
            Logger.error("Element is null for $githubPath")
            return
        }

        val jsonObject = element.asJsonObject

        if (jsonObject == null) {
            Logger.error("JsonObject was null for $githubPath")
            return
        }

        var tag = jsonObject["tag"].asString
        if (tag == "latest") tag = pullLatestReleaseTag()

        if (tag == null) {
            Logger.error("Tag was invalid for $githubPath")
            return
        }

        var fileName = jsonObject["filename"].asString
        fileName = fileName.replace("\${TAG}", tag)

        val validFiles = pullAvailableFiles(tag)
        var file: JsonObject? = null

        for (validFile in validFiles) {
            val name = validFile.asJsonObject["name"].asString ?: continue
            if (name != fileName) continue

            file = validFile.asJsonObject
        }

        if (file == null) {
            Logger.error("File is null for $githubPath")
            return
        }

        val oldFile = File("./$destination/${cache.checkVersion(githubPath)}")

        if (cache.checkVersion(githubPath) == "$tag-$fileName" && oldFile.exists()) {
            Logger.log("Skipping download for $githubPath")
            return
        }

        if (oldFile.exists()) {
            Logger.log("Deleting old version of $githubPath")
            if (!oldFile.delete()) {
                Logger.warning("Failed to delete old version of $githubPath")
            }
        }

        downloadFile(file, tag)
    }

    private fun pullLatestReleaseTag(): String? {
        val latest = getFromGithub("releases/latest").asJsonObject ?: return null
        return latest["tag_name"]?.asString
    }

    private fun pullAvailableFiles(tag: String): JsonArray {
        val jsonArray = getFromGithub("releases").asJsonArray

        for (jsonElement in jsonArray) {
            val tagName = jsonElement.asJsonObject["tag_name"] ?: continue

            if (tagName.asString == tag) {
                return jsonElement.asJsonObject["assets"].asJsonArray
            }
        }

        return JsonArray()
    }

    private fun downloadFile(fileObject: JsonObject, tag: String) {
        val fileName = fileObject["name"].asString
        val path = fileObject["browser_download_url"].asString

        Logger.log("Downloading $fileName...")

        try {
            val url = URL(path)
            val urlConnection = url.openConnection()
            urlConnection.setRequestProperty("Authorization", "Bearer ${configuration.getValue("token").asString}")

            val fos = FileOutputStream("./$destination/$tag-$fileName")

            val size = urlConnection.contentLength.toDouble()
            val inputStream = urlConnection.getInputStream()

            val buffer = ByteArray(1024)
            var bytesRead: Int
            var totalBytesRead = 0.0

            var lastPrinted = -1

            while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                fos.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead.toDouble()
                val percent = Math.round(totalBytesRead / size * 100).toInt()

                if (percent % 5 == 0 && lastPrinted != percent) {
                    Logger.debug("$percent - $fileName [${Math.round(totalBytesRead/1000)}KB/${Math.round(size/1000)}KB]")
                    lastPrinted = percent
                }
            }

            fos.close()

            Logger.log("Successfully downloaded $fileName")
            main.didDownload = true
            cache.insertVersion(githubPath, "$tag-$fileName")
        } catch (e: IOException) {
            Logger.error("Failed to download $fileName")
            e.printStackTrace()
        }
    }

    private fun getFromGithub(url: String): JsonElement {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/$githubPath/$url"))
            .header("Authorization", "Bearer ${configuration.getValue("token").asString}")
            .GET()
            .build()

        try {
            return Gson().fromJson(
                client.send(request, HttpResponse.BodyHandlers.ofString()).body(),
                JsonElement::class.java
            )
        } catch (exception: IOException) {
            Logger.error("Uh oh! Something went wrong when getting the response from GitHub. Could it be down?")
            Logger.error("Failed to download $githubPath")

            throw exception
        } catch (exception: InterruptedException) {
            Logger.error("Uh oh! Something went wrong when getting the response from GitHub.")
            Logger.error("Failed to download $githubPath")

            throw exception
        }
    }
}
