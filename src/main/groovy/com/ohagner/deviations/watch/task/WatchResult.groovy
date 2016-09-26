package com.ohagner.deviations.watch.task

import java.time.Duration

class WatchResult {

    WatchExecutionStatus status
    List<String> messages = []
    Duration executionTime

    def addMessage(def message) {
        messages.add(message)
    }

}

