package com.ohagner.deviations.repository

import com.mongodb.*
import com.mongodb.util.JSON
import com.ohagner.deviations.domain.Watch
import groovy.util.logging.Slf4j

import java.time.LocalDateTime
import java.time.ZoneId

@Slf4j
final class WatchRepository {

    DBCollection watches
    IncrementalCounter counter

    WatchRepository(DBCollection collection, IncrementalCounter counter) {
        this.counter = counter
        this.watches = collection
        log.info "WatchRepository initialized"
    }

    Watch findById(long id) {
        log.debug "Retrieving watch for user id $id"
        DBObject watchObject = watches.findOne(new BasicDBObject(id:id))
        return Watch.fromJson(JSON.serialize(watchObject))
    }

    List<Watch> findByUsername(String username) {
        log.debug "Retrieving watches for user $username"
        DBCursor cursor = watches.find(new BasicDBObject(username: username))
        List<Watch> watches = cursor.iterator().collect { Watch.fromJson(JSON.serialize(it)) }
        return watches
    }

    Watch findByUsernameAndName(String username, String watchName) {
        DBObject watchObject = watches.findOne(new BasicDBObject(name:watchName, username:username))
        return Watch.fromJson(JSON.serialize(watchObject))
    }

    List<Watch> retrieveAll() {
        DBCursor cursor = watches.find()
        List<Watch> watches = cursor.iterator().collect { Watch.fromJson(JSON.serialize(it)) }
        return watches
    }

    List<Watch> retrieveNextToProcess(int max) {
        DBCursor cursor = watches.find().sort(new BasicDBObject(lastProcessed: 1)).limit(max)
        return cursor.iterator().collect { Watch.fromJson(JSON.serialize(it)) }
    }

    Watch create(Watch watch) {
        long generatedId = counter.getAndIncrement()
        watch.id = generatedId
        watch.created = LocalDateTime.now(ZoneId.of("Europe/Paris"))
        DBObject mongoWatch = JSON.parse(watch.toJson())
        WriteResult result = watches.insert(mongoWatch)
        if (result.getN() == 1) {
            log.info "Successfully created watch"
        }
        return findByUsernameAndName(watch.username, watch.name)
    }

    Watch update(Watch watch) {
        watch.lastUpdated = LocalDateTime.now(ZoneId.of("Europe/Paris"))
        DBObject mongoWatch = JSON.parse(watch.toJson())
        WriteResult result = watches.update(new BasicDBObject(id:watch.id), mongoWatch)
        if (result.getN() == 1) {
            log.info "Successfully updated watch"
        }
        return findById(watch.id)
    }

    void delete(String username, String watchName) {
        watches.remove(new BasicDBObject(name:watchName, username:username))
    }

    boolean exists(String username, String watchName) {
        return watches.count(new BasicDBObject(name:watchName, username:username)) > 0
    }

}
