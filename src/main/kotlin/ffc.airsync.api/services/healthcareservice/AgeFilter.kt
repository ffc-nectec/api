package ffc.airsync.api.services.healthcareservice

import ffc.airsync.api.services.util.equal
import org.bson.Document

class AgeFilter(val start: Int? = null, val end: Int? = null) : Filter {
    init {
        require(start != null || end != null)
    }

    override fun get(): Document {
        return if (start == null) {
            "age" equal ""
        } else if (end == null) {
            "age" equal ""
        } else {
            "age" equal ""
        }
    }
}
