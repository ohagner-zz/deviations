package com.ohagner.deviations.worker.watch.domain

import com.google.common.base.Stopwatch
import com.ohagner.deviations.api.deviation.domain.Deviation
import groovy.json.JsonOutput

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import static com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus.*

class WatchProcessingResult {

    static WatchProcessingResult newInstance() {
        return new WatchProcessingResult()
    }

    private WatchProcessingResult() {
        this.status = STARTED
        timer = Stopwatch.createStarted()
    }

    private Stopwatch timer

    WatchProcessingStatus status
    List<String> messages = []

    Set<Deviation> matchingDeviations = []

    Duration getExecutionTime() {
        return Duration.ofMillis(timer.elapsed(TimeUnit.MILLISECONDS))
    }

    void addMessage(String message) {
        messages.add(message)
    }


    @Override
    public String toString() {
        return JsonOutput.toJson {
            status(status)
            message(messages.join(". "))
            executionTime(getExecutionTime().toMillis() + " ms")
            matchingDeviations(matchingDeviations ? matchingDeviations.collect { it.id }.join(". ") : "[]")
        }
    }
}

