package com.ohagner.deviations.api.watch

import com.mongodb.*
import com.mongodb.util.JSON
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.time.LocalDateTime

import static com.ohagner.deviations.config.Constants.ZONE_ID

@Slf4j
@CompileStatic
class MongoWatchRepository implements WatchRepository {

    DBCollection watches
    IncrementalCounter counter

    MongoWatchRepository(DBCollection collection, IncrementalCounter counter) {
        this.counter = counter
        this.watches = collection
        log.info "WatchRepository initialized"
    }

    Optional<Watch> findById(long id) {
        log.debug "Retrieving watch with id $id"
        optionalFrom watches.findOne(new BasicDBObject(id:id))
    }

    List<Watch> findByUsername(String username) {
        log.debug "Retrieving watches for user $username"
        DBCursor cursor = watches.find(new BasicDBObject(username: username))
        List<Watch> watches = cursor.iterator().collect { Watch.fromJson(JSON.serialize(it)) }
        return watches
    }

    Optional<Watch> findByUsernameAndId(String username, long id) {
        log.debug "Looking for watch for user $username with id $id"
        optionalFrom watches.findOne(new BasicDBObject(username:username, id:id))
    }

    List<Watch> retrieveAll() {
        DBCursor cursor = watches.find()
        List<Watch> watches = cursor.iterator().collect { Watch.fromJson(JSON.serialize(it)) }
        return watches
    }

    List<Watch> retrieveRange(int pageNumber, int maxNumPerPage) {
        int toSkip = pageNumber > 0 ? ((pageNumber-1) * maxNumPerPage) : 0
        log.debug "Retrieving ranged with $pageNumber"
        DBCursor cursor = watches.find().skip(toSkip).limit(maxNumPerPage)
        return cursor.iterator()
                .collect { Watch.fromJson(JSON.serialize(it)) }
    }

    Watch create(Watch watch) {
        long generatedId = counter.getAndIncrement()
        watch.id = generatedId
        watch.created = LocalDateTime.now(ZONE_ID)
        DBObject mongoWatch = JSON.parse(watch.toJson()) as DBObject

        WriteResult result = watches.insert(mongoWatch)
        if (result.getN() == 1) {
            log.debug "Successfully created watch"
        }//TODO: Throw some kind of exception otherwise
        return findByUsernameAndId(watch.username, watch.id).get()
    }

    Watch update(Watch watch) {
        watch.lastProcessed = LocalDateTime.now(ZONE_ID)
        DBObject mongoWatch = JSON.parse(watch.toJson()) as DBObject
        WriteResult result = watches.update(new BasicDBObject(id:watch.id), mongoWatch)
        if (result.getN() == 1) {
            log.debug "Successfully updated watch"
        }
        return findById(watch.id).get()
    }

    Optional<Watch> delete(String username, long id) {
        log.info "Deleting watch for user $username with id $id"
        optionalFrom watches.findAndRemove(new BasicDBObject(id:id, username:username))
    }

    boolean exists(String username, long id) {
        return watches.count(new BasicDBObject(id:id, username:username)) > 0
    }

    private Optional<Watch> optionalFrom(DBObject dbObject) {
        if(dbObject) {
            return Optional.of(Watch.fromJson(JSON.serialize(dbObject)))
        } else {
            return Optional.empty()
        }
    }

}
