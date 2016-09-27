package com.ohagner.deviations.repository

import com.ohagner.deviations.domain.Deviation
import groovy.util.logging.Slf4j

import java.time.Duration
import java.time.LocalDateTime

@Slf4j
class CachedDeviationRepository implements DeviationRepository {

    Duration timeToCache

    DeviationRepository source

    LocalDateTime lastUpdated

    List<Deviation> cachedResponse

    CachedDeviationRepository(DeviationRepository source, Duration timeToCache) {
        this.source = source
        this.timeToCache = timeToCache
    }

    @Override
    List<Deviation> retrieveAll() {
        if(cachedResponse == null) {
            cachedResponse = source.retrieveAll()
            lastUpdated = LocalDateTime.now()
        }

        Duration timeSinceUpdate = Duration.between(lastUpdated, LocalDateTime.now())
        if (timeSinceUpdate.compareTo(timeToCache) > 0) {
            log.info "Updating cached deviations"
            cachedResponse = source.retrieveAll()
            lastUpdated = LocalDateTime.now()
        }
        return cachedResponse
    }

}
