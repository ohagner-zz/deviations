package com.ohagner.deviations.repository

import com.mongodb.*
import com.mongodb.util.JSON
import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.Watch
import groovy.util.logging.Slf4j

@Slf4j
final class WatchRepository {

    DBCollection watches

    WatchRepository(DBCollection collection) {
        log.info "WatchRepository initialized"
        watches = collection
    }

    Watch findByUsername(String username) {
        log.debug "Retrieving watches for user $username"
        DBCursor cursor = watches.find(new BasicDBObject(username: username))
        List<Watch> watches = cursor.iterator().collect { Watch.fromJson(JSON.serialize(it)) }
        return watches
    }

    List<Watch> retrieveAll() {
        DBCursor cursor = watches.find()
        List<Watch> watches = cursor.iterator().collect { Watch.fromJson(JSON.serialize(it)) }
        return watches
    }

    Watch create(Watch watch) {
        DBObject mongoWatch = JSON.parse(watch.toJson())
        WriteResult result = watches.insert(mongoWatch)
        if (result.getN() == 1) {
            log.info "Successfully created watch"
        }
        return findByUsername(watch.user)
    }

    void delete(Watch watch) {
        watches.remove(JSON.parse(watch.toJson()))
    }

}
