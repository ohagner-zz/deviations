package com.ohagner.deviations.worker.watch.domain

import com.ohagner.deviations.api.deviation.domain.Deviation
import groovy.json.JsonOutput

import java.time.Duration

class WatchProcessingResult {

    WatchProcessingStatus status
    List<String> messages = []
    Duration executionTime

    Set<Deviation> matchingDeviations = []

    def addMessage(def message) {
        messages.add(message)
    }


    @Override
    public String toString() {
        return JsonOutput.toJson {
            status(status)
            message(messages.join(". "))
            executionTime(executionTime.toMillis() + " ms")
            matchingDeviations(matchingDeviations ? matchingDeviations.collect { it.id }.join(",") : "[]")
        }
    }
}

