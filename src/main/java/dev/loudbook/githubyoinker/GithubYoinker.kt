package dev.loudbook.githubyoinker

import java.io.*
import kotlin.system.exitProcess


class GithubYoinker {
    var didDownload = false

    fun initiate() {
        val configuration = Configuration()
        val cache = Cache()

        val array = configuration.getValue("files").asJsonArray

        for (jsonElement in array) {
            Downloader(
                configuration,
                jsonElement.asJsonObject["repo"].asString,
                this,
                cache
            )
        }

        cache.saveCache()

        if (didDownload) {
            Logger.log("Successfully downloaded all files!")
        }

        val startup = configuration.getValue("post").asString

        val processBuilder = ProcessBuilder(startup.split(" "))

        Logger.log("Starting server process...")
        val process = processBuilder.start()

        handleConsoleOutput(process.inputStream)
        handleConsoleInput(process.outputStream)

        process.waitFor()

        exitProcess(0)
    }

    private fun handleConsoleOutput(inputStream: InputStream) {
        val thread = Thread {
            try {
                var data: Int
                while ((inputStream.read().also { data = it }) != -1) {
                    System.out.write(data)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        thread.start()
    }

    private fun handleConsoleInput(outputStream: OutputStream) {
        val thread = Thread {
            try {
                val consoleReader = BufferedReader(InputStreamReader(System.`in`))
                val writer = PrintWriter(outputStream, true)

                var input: String?
                while (consoleReader.readLine().also { input = it } != null) {
                    writer.println(input)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        thread.start()
    }
}
