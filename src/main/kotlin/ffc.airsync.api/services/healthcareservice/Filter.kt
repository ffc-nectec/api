package ffc.airsync.api.services.healthcareservice

import org.bson.Document

interface Filter {
    fun get(): Document
}
