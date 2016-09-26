package com.ohagner.deviations.repository

import com.mongodb.BasicDBObject
import com.mongodb.DBCollection

final class IncrementalCounter {

    DBCollection countersCollection
    String counterName

    public static IncrementalCounter createCounter(String counterName) {

    }

    public IncrementalCounter(DBCollection countersCollection, String counterName) {
        this.countersCollection = countersCollection
        this.counterName = counterName
    }



    public static void createCounterDocument() {

        BasicDBObject document = new BasicDBObject();
        document.append("_id", "userid");
        document.append("seq", 0);
        countersCollection.insert(document);
    }

}
