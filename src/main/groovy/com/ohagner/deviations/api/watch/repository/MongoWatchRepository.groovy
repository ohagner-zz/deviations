package com.ohagner.deviations.api.watch.repository

import com.mongodb.*
import com.mongodb.util.JSON
import com.ohagner.deviations.api.watch.domain.Watch
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import ratpack.exec.Blocking
import ratpack.exec.Promise

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

    @Override
    Promise<Optional<Watch>> findById(long id) {
        log.debug "Retrieving watch with id $id"
        return Promise.value(optionalFrom(watches.findOne(new BasicDBObject(id:id))))
    }

    @Override
    Promise<List<Watch>> findByUsername(String username) {
        Blocking.get {
            log.debug "Retrieving watches for user $username"
            DBCursor cursor = watches.find(new BasicDBObject(username: username))
            List<Watch> watches = cursor.iterator().collect { Watch.fromJson(JSON.serialize(it)) }
            log.debug "Found ${watches.size()} number of watches for user $username"
            return watches
        }
    }

    @Override
    Promise<Optional<Watch>> findByUsernameAndId(String username, long id) {
        Blocking.get {
            log.debug "Looking for watch for user $username with id $id"
            return optionalFrom(watches.findOne(new BasicDBObject(username: username, id: id)))
        }
    }

    @Override
    Promise<List<Watch>> retrieveAll() {
        Blocking.get {
            DBCursor cursor = watches.find()
            return cursor.iterator().collect { Watch.fromJson(JSON.serialize(it)) }
        }
    }

    @Override
    List<Watch> retrieveRange(int pageNumber, int maxNumPerPage) {
        int toSkip = pageNumber > 0 ? ((pageNumber-1) * maxNumPerPage) : 0
        log.debug "Retrieving ranged with $pageNumber"
        DBCursor cursor = watches.find().skip(toSkip).limit(maxNumPerPage)
        return cursor.iterator()
                .collect { Watch.fromJson(JSON.serialize(it)) }
    }

    @Override
    Promise<Optional<Watch>> create(Watch watch) {
        Blocking.get {
            long generatedId = counter.getAndIncrement()
            watch.id = generatedId
            watch.created = LocalDateTime.now(ZONE_ID)
            DBObject mongoWatch = JSON.parse(watch.toJson()) as DBObject

            watches.insert(mongoWatch)
            return optionalFrom(watches.findOne(new BasicDBObject(username: watch.username, id: watch.id)))
        }
    }

    @Override
    Promise<Watch> update(Watch watch) {
        Blocking.get {
            watch.lastProcessed = LocalDateTime.now(ZONE_ID)
            DBObject mongoWatch = JSON.parse(watch.toJson()) as DBObject
            watches.update(new BasicDBObject(id: watch.id), mongoWatch)
            return optionalFrom(watches.findOne(new BasicDBObject(id: watch.id))).get()
        }
    }

    @Override
    Promise<Optional<Watch>> delete(String username, long id) {
        Blocking.get {
            log.info "Deleting watch for user $username with id $id"
            return optionalFrom(watches.findAndRemove(new BasicDBObject(id: id, username: username)))
        }
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
