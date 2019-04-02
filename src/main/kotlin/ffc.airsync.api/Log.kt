package ffc.airsync.api

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

inline fun <reified T> getLoggerC(clazz: T): Logger {
    return LogManager.getLogger(T::class)
}

fun Any.getLogger(): Logger {
    return LogManager.getLogger(this::class.java)
}
