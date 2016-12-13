package com.ohagner.deviations.api.watch.repository

import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import groovy.util.logging.Slf4j

@Slf4j
final class IncrementalCounter {

    public static final String SEQUENCE_PROPERTY = "seq"
    DBCollection countersCollection
    String counterName

    public static IncrementalCounter createCounter(DBCollection countersCollection, String counterName) {
        IncrementalCounter instance = new IncrementalCounter(countersCollection, counterName)
        instance.initialize()
        return instance
    }

    private IncrementalCounter(DBCollection countersCollection, String counterName) {
        this.countersCollection = countersCollection
        this.counterName = counterName
    }

    public long getAndIncrement() {
        BasicDBObject searchQuery = new BasicDBObject("_id", counterName)
        BasicDBObject increase = new BasicDBObject(SEQUENCE_PROPERTY, 1)
        BasicDBObject updateQuery = new BasicDBObject('$inc', increase)
        BasicDBObject result = countersCollection.findAndModify(searchQuery, updateQuery)
        log.debug "Incremented id, returning ${result.get(SEQUENCE_PROPERTY)}"
        return result.get(SEQUENCE_PROPERTY) as long
    }

    private void initialize() {
        if(countersCollection.count(new BasicDBObject("_id":counterName)) == 0) {
            BasicDBObject document = new BasicDBObject()
            document.append("_id", counterName)
            document.append(SEQUENCE_PROPERTY, 0)
            countersCollection.insert(document)
        }
    }


}
