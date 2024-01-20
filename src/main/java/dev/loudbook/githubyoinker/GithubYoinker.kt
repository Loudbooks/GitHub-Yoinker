package dev.loudbook.githubyoinker

import kotlinx.coroutines.*
import java.io.*
import kotlin.system.exitProcess


class GithubYoinker {
    private val instance = this

    var didDownload = false

    suspend fun initiate() {
        val configuration = Configuration()
        val cache = Cache()

        val array = configuration.getValue("files").asJsonArray
        val path = configuration.getValue("destinationDirectory").asString
        val debug = configuration.getValue("debug").asBoolean

        Logger.debug = debug

        val file = File(path)
        file.mkdir()

        val jobs = mutableListOf<Job>()

        coroutineScope {
            for (jsonElement in array) {
                val job = async(Dispatchers.IO) {
                    println(Thread.currentThread().name)
                    Downloader(
                        configuration,
                        jsonElement.asJsonObject["repo"].asString,
                        instance,
                        cache
                    )
                }

                jobs.add(job)
            }
        }

        jobs.joinAll()

        cache.saveCache()

        if (didDownload) {
            Logger.log("Successfully downloaded all files!")
        }

        val startup = configuration.getValue("post").asString
        val processBuilder = ProcessBuilder(startup.split(" "))

        Logger.log("Starting post process...")
        val process = withContext(Dispatchers.IO) {
            processBuilder.start()
        }

        handleConsoleOutput(process.inputStream)
        handleConsoleInput(process.outputStream)

        withContext(Dispatchers.IO) {
            process.waitFor()
        }

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
