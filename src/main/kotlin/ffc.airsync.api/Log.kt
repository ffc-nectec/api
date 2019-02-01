package ffc.airsync.api

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

fun Any.getLogger(): Logger {
    return LogManager.getLogger(this::class.java)
}
