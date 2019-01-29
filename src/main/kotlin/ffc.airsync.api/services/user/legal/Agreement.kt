package ffc.airsync.api.services.user.legal

import org.joda.time.DateTime

data class Agreement(
    val version: String,
    val agreeTime: DateTime = DateTime.now()
)



