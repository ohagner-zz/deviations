package com.ohagner.deviations.api.user.repository

import com.mongodb.Block
import com.mongodb.async.SingleResultCallback
import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.ohagner.deviations.api.error.UserNotFoundException
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.service.security.HashGenerator
import groovy.util.logging.Slf4j
import org.bson.Document
import org.bson.conversions.Bson
import ratpack.exec.Promise

import static com.mongodb.client.model.Filters.eq

@Slf4j
class MongoUserRepository implements UserRepository {

    MongoCollection<Document> users

    MongoUserRepository(MongoCollection<Document> collection) {
        this.users = collection
        log.info "UserRepository initialized"
    }

    Promise<User> findByUsername(String username) {
        log.debug "Retrieving data for user $username"

        findOne(eq('credentials.username', username)).map { Document userDocument ->
            User.fromJson(userDocument.toJson())
        }
    }

    Promise<User> findByApiToken(String apiToken) {
        if(!apiToken) {
            return Promise.value(null)
        }
        findOne(eq('credentials.apiToken.value', apiToken)).map { Document userDocument ->
            User.fromJson(userDocument.toJson())
        }
    }

    Promise<List<User>> retrieveAll() {
        findAll().map { userDocuments ->
            userDocuments.collect { User.fromJson(it.toJson())}
        }
    }

    Promise<User> create(User user, String password) {
        user.credentials.passwordSalt = HashGenerator.createSalt()
        user.credentials.passwordHash = HashGenerator.generateHash(password, user.credentials.passwordSalt)

        Document userDocument = Document.parse(user.toJson())
        insert(userDocument).flatMap {
            findByUsername(user.credentials.username)
        }

    }

    Promise<User> delete(User user) {
        deleteOne(eq('credentials.username', user.credentials.username)).map { Document userDocument ->
            User.fromJson(userDocument.toJson())
        }

    }

    Promise<User> update(String username, User update) {
        updateDocument(eq('credentials.username', username), Document.parse(update.toJson())).map { Document userDocument ->
            User.fromJson(userDocument.toJson())
        }
    }

    Promise<Boolean> userExists(String username) {
        return exists(eq('credentials.username': username))
    }

    private Promise<Boolean> exists(Bson filter) {
        return Promise.async { down ->
            users.count(filter, new SingleResultCallback<Long>() {
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
            users.insertOne(document, new SingleResultCallback<Void>() {
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
            log.info "Update: ${update.dump()}"
            users.findOneAndReplace(filter, update, new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER), new SingleResultCallback<Document>() {
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
            users.find(filter).first(new SingleResultCallback<Document>() {
                @Override
                void onResult(Document result, Throwable t) {
                    if(t) {
                        down.error(t)
                    } else if (!result) {
                        down.error(new UserNotFoundException("No user found for query: ${filter.toString()}"))
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

            users.find(filter).forEach(addResultsToList, finished)
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

            users.find().forEach(addResultsToList, onFinished)
        }
    }

    private Promise<Document> deleteOne(Bson filter) {
        Promise.async { down ->
            users.findOneAndDelete(filter, new SingleResultCallback<Document>() {
                @Override
                void onResult(Document result, Throwable t) {
                    if(t) {
                        down.error(t)
                    }
                    log.info "Deleted ${result.dump()}"
                    down.success(result)
                }
            })
        }
    }

}
