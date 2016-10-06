package com.ohagner.deviations.repository

import static com.ohagner.deviations.config.Constants.ZONE_ID
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
            lastUpdated = LocalDateTime.now(ZONE_ID)
        }

        Duration timeSinceUpdate = Duration.between(lastUpdated, LocalDateTime.now(ZONE_ID))
        if (timeSinceUpdate.compareTo(timeToCache) > 0) {
            log.info "Updating cached deviations"
            cachedResponse = source.retrieveAll()
            lastUpdated = LocalDateTime.now(ZONE_ID)
        }
        return cachedResponse
    }


}
