package com.ruko.system.presentation.data

data class QueueState(
    var businessName: String = "",
    var queueLength: Int = 0,
    var estimatedWait: Int = 0,
    var position: Int = 0
)