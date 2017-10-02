package com.ohagner.deviations.api.watch.repository

import com.mongodb.*
import com.mongodb.async.SingleResultCallback
import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.util.JSON
import com.ohagner.deviations.api.error.UserNotFoundException
import com.ohagner.deviations.api.error.WatchNotFoundException
import com.ohagner.deviations.api.watch.domain.Watch
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.bson.Document
import org.bson.conversions.Bson
import ratpack.exec.Blocking
import ratpack.exec.Promise

import java.time.LocalDateTime

import static com.mongodb.client.model.Filters.and
import static com.mongodb.client.model.Filters.eq
import static com.ohagner.deviations.config.Constants.ZONE_ID

@Slf4j
//@CompileStatic
class MongoWatchRepository implements WatchRepository {

    MongoCollection<Document> watches
    IncrementalCounter counter

    MongoWatchRepository(MongoCollection<Document> collection, IncrementalCounter counter) {
        this.counter = counter
        this.watches = collection
        log.info "WatchRepository initialized"
    }

    @Override
    Promise<Optional<Watch>> findById(long id) {
        findOne(eq('id', id)).map{ watchDocument ->
            Optional.of(Watch.fromJson(watchDocument.toJson()))
        }.mapError(WatchNotFoundException) {
            Optional.empty()
        }
    }

    @Override
    Promise<List<Watch>> findByUsername(String username) {
        findAllMatching(eq('username', username)).map { watchDocuments ->
            watchDocuments.collect { Watch.fromJson(it.toJson())}
        }
    }

    @Override
    Promise<Optional<Watch>> findByUsernameAndId(String username, long id) {
        findOne(and(eq('id', id), eq('username', username))).map { watchDocument ->
            Optional.of(Watch.fromJson(watchDocument.toJson()))
        }.mapError(WatchNotFoundException) {
            Optional.empty()
        }
    }

    @Override
    Promise<List<Watch>> retrieveAll() {
        findAll().map { watchDocuments ->
            watchDocuments.collect { Watch.fromJson(it.toJson())}
        }
    }

    @Override
    List<Watch> retrieveRange(int pageNumber, int maxNumPerPage) {
        //TODO: Return a Promise
        int toSkip = pageNumber > 0 ? ((pageNumber - 1) * maxNumPerPage) : 0
        log.debug "Retrieving ranged with $pageNumber"
        findAllPaged(toSkip, maxNumPerPage).to {  watchesDocumentsPromise ->
            List<Watch> watches
            watchesDocumentsPromise.then { watchDocuments ->
                watches = watchDocuments.collect { Document watchDocument -> Watch.fromJson(watchDocument.toJson())}
            }
            return watches
        }

    }

    @Override
    Promise<Watch> create(Watch watch) {
        counter.getAndIncrement().map { generatedId ->
            log.info "Generated id : $generatedId"
            watch.id = generatedId
            watch.created = LocalDateTime.now(ZONE_ID)
            insert(Document.parse(watch.toJson())).then { Boolean result ->
                log.info "Created watch successfully: $result"
            }
            return generatedId
        }.flatMap { generatedId->
            findOne(eq("id", generatedId))
        }.map { watchDocument ->
            Watch.fromJson(watchDocument.toJson())
        }
    }

    @Override
    Promise<Watch> update(Watch watch) {

        updateDocument(eq('id', watch.id), Document.parse(watch.toJson())).map { Document watchDocument ->
            Watch.fromJson(watchDocument.toJson())
        }
    }

    @Override
    Promise<Optional<Watch>> delete(String username, long id) {

        deleteOne(and(eq('username', username), eq('id', id)))
            .onError(WatchNotFoundException) {
               Optional.empty()
            }.map { watchDocument ->
                Optional.of(Watch.fromJson(watchDocument.toJson()))
            }
    }

    private Promise<Boolean> insert(Document document) {
        return Promise.async { down ->
            watches.insertOne(document, new SingleResultCallback<Void>() {
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

    private Promise<Document> updateDocument(Bson filter, Document update) {
        return Promise.async { down ->
            watches.findOneAndReplace(filter, update, new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER), new SingleResultCallback<Document>() {
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

    private Promise<Document> findOne(Bson filter) {
        return Promise.async { down ->
            watches.find(filter).first(new SingleResultCallback<Document>() {
                @Override
                void onResult(Document result, Throwable t) {
                    if(t) {
                        down.error(t)
                    } else if (!result) {
                        down.error(new WatchNotFoundException("No watch found for query: ${filter.toString()}"))
                    }
                    down.success(result)
                }
            })
        }
    }

    private Promise<List<Document>> findAllMatching(Bson filter) {
        return Promise.async { down ->
            def resultList = []

            Block<Document> addResultsToList = new Block<Document>() {
                @Override
                public void apply(final Document document) {
                    resultList.add(document)
                }
            }

            def finished = { Void result, Throwable t ->
                if(t) {
                    down.error(t)
                }
                down.success(resultList)
            }

            watches.find(filter).forEach(addResultsToList, finished)
        }
    }

    private Promise<List<Document>> findAllPaged(int limit, int toSkip) {
        return Promise.async { down ->
            def resultList = []

            Block<Document> addResultsToList = new Block<Document>() {
                @Override
                public void apply(final Document document) {
                    resultList.add(document)
                }
            }

            def finished = { Void result, Throwable t ->
                if(t) {
                    down.error(t)
                }
                down.success(resultList)
            }

            watches.find().skip(toSkip).limit(limit).forEach(addResultsToList, finished)
        }
    }

    private Promise<List<Document>> findAll() {
        return Promise.async { down ->
            def resultList = []

            Block<Document> addResultsToList = new Block<Document>() {
                @Override
                public void apply(final Document document) {
                    resultList.add(document)
                }
            }

            SingleResultCallback<Void> onFinished = new SingleResultCallback<Void>() {
                @Override
                public void onResult(final Void result, final Throwable t) {
                    if(t) {
                        down.error(t)
                    }
                    down.success(resultList)
                }
            };

            watches.find().forEach(addResultsToList, onFinished)
        }
    }

    private Promise<Document> deleteOne(Bson filter) {
        Promise.async { down ->
            watches.findOneAndDelete(filter, new SingleResultCallback<Document>() {
                @Override
                void onResult(Document result, Throwable t) {
                    if(t) {
                        down.error(t)
                    } else if (!result) {
                        down.error(new WatchNotFoundException("No watch found for filter ${filter.toString()}"))
                    }
                    down.success(result)
                }
            })
        }
    }
    
}
