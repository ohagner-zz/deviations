package com.ohagner.deviations.api.deviation.repository

import com.ohagner.deviations.api.deviation.domain.Deviation
import ratpack.exec.Promise

import static com.ohagner.deviations.config.Constants.ZONE_ID

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
    Promise<List<Deviation>> retrieveAll() {
        if(cachedResponse == null || cacheHasExpired()) {
            log.info "Updating cached deviations. Cache timeout is ${timeToCache.toMinutes()} minutes"
            source.retrieveAll().map() { List<Deviation> deviations ->
                lastUpdated = LocalDateTime.now(ZONE_ID)
                cachedResponse = deviations
            }

        } else {
            return Promise.value(cachedResponse)
        }


    }

    private boolean cacheHasExpired() {
        Duration timeSinceUpdate = Duration.between(lastUpdated, LocalDateTime.now(ZONE_ID))
        return timeSinceUpdate.compareTo(timeToCache) > 0
    }

}
