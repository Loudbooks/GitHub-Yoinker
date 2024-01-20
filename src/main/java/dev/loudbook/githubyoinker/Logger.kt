package dev.loudbook.githubyoinker

import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger


object Logger {
    private var logger: Logger = Logger.getGlobal()

    init {
        System.setProperty("java.util.logging.SimpleFormatter.format",
            "%5\$s%6\$s%n")
    }

    fun log(message: String) {
        logger.log(LogRecord(Level.INFO, buildLog(Color.GREEN.toString() + message)))
    }

    fun rawLog(message: String?) {
        logger.log(LogRecord(Level.INFO, message))
    }

    fun warning(message: String) {
        logger.log(LogRecord(Level.WARNING, buildLog(Color.YELLOW.toString() + message)))
    }

    fun error(message: String) {
        logger.log(LogRecord(Level.SEVERE, buildLog(Color.RED.toString() + message)))
    }

    fun debug(message: String) {
        logger.log(LogRecord(Level.INFO, buildLog(Color.BLUE.toString() + "[Debug] " + message)))
    }

    private fun buildLog(message: String): String {
        return "[GithubYoinker] " + message + Color.RESET
    }

    enum class Color(private val color: String) {
        RED("\u001B[31m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),
        GREEN("\u001B[32m"),
        RESET("\u001B[0m");

        override fun toString(): String {
            return color
        }
    }
}