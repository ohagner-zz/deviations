package com.ohagner.deviations.api.watch.repository

import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import com.mongodb.async.SingleResultCallback
import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import groovy.util.logging.Slf4j
import org.bson.Document
import org.bson.conversions.Bson
import ratpack.exec.Execution
import ratpack.exec.Promise

import static com.mongodb.client.model.Filters.eq
import static com.mongodb.client.model.Updates.inc


@Slf4j
final class IncrementalCounter {

    public static final String SEQUENCE_PROPERTY = "seq"
    MongoCollection<Document> countersCollection
    String counterName

    public static IncrementalCounter createCounter(MongoCollection<Document> countersCollection, String counterName) {
        IncrementalCounter instance = new IncrementalCounter(countersCollection, counterName)
        instance.initialize()
        return instance
    }

    private IncrementalCounter(MongoCollection<Document> countersCollection, String counterName) {
        this.countersCollection = countersCollection
        this.counterName = counterName
    }

    public Promise<Long> getAndIncrement() {
        update(eq("_id",counterName), inc(SEQUENCE_PROPERTY, 1))
            .map { Document document ->
                document.getInteger(SEQUENCE_PROPERTY).longValue()
            }
    }

    private void initialize() {
        Execution.fork().start { execution ->
            exists(eq("_id",counterName))
                    .route({ counterExists -> counterExists == false }) {
                Document counter = new Document(["_id": counterName, SEQUENCE_PROPERTY: 0])
                insert(counter)
            }.then {
                log.info "Counter $counterName initialized"
            }
        }

    }

    private Promise<Boolean> exists(Bson filter) {
        return Promise.async { down ->
            countersCollection.count(filter, new SingleResultCallback<Long>() {
                @Override
                void onResult(Long result, Throwable t) {
                    if(t) {
                        down.error(t)
                    }
                    down.success(result > 0)
                }
            })
        }
    }

    private Promise<Boolean> insert(Document document) {
        return Promise.async { down ->
            countersCollection.insertOne(document, new SingleResultCallback<Void>() {
                @Override
                void onResult(Void result, Throwable t) {
                    if(t) {
                        down.error(t)
                    }
                    down.success(Boolean.TRUE)
                }
            })
        }
    }

    private Promise<Document> update(Bson filter, Bson update) {
        return Promise.async { down ->
            countersCollection.findOneAndUpdate(filter, update, new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER), new SingleResultCallback<Document>() {
                @Override
                void onResult(Document result, Throwable t) {
                    if(t) {
                        down.error(t)
                    } else if(result == null) {
                        down.error(new Exception("Document not found with filter ${filter.toString()}"))
                    } else {
                        log.info "Update result: ${result.dump()}"
                        down.success(result)
                    }

                }
            })
        }
    }

}
