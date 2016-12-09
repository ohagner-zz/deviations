package com.ohagner.deviations.api.user

import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.WriteResult
import com.mongodb.util.JSON
import com.ohagner.deviations.api.user.security.HashGenerator
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import ratpack.exec.Blocking
import ratpack.exec.Promise

@Slf4j
class MongoUserRepository implements UserRepository {

    DBCollection users

    MongoUserRepository(DBCollection collection) {
        this.users = collection
        log.info "UserRepository initialized"
    }

    Optional<User> findByUsername(String username) {
        log.debug "Retrieving data for user $username"
        DBObject userObject = users.findOne(new BasicDBObject('credentials.username': username))
        if (userObject) {
            return Optional.of(User.fromJson(JSON.serialize(userObject)))
        } else {
            Optional.empty()
        }
    }

    Promise<User> findByApiToken(String apiToken) {
        if(!apiToken) {
            return Promise.value(null)
        }
        Blocking.get {
            DBObject userObject = users.findOne(new BasicDBObject('credentials.apiToken.value': apiToken))
            return userObject ? User.fromJson(JSON.serialize(userObject)) : null
        }
    }

    List<User> retrieveAll() {
        DBCursor cursor = users.find()
        List<User> users = cursor.iterator().collect { User.fromJson(JSON.serialize(it)) }
        users.each {
            log.info "Retrieved: ${JsonOutput.prettyPrint(it.toJson())}"
        }
        return users
    }

    User create(User user, String password) {
        user.credentials.passwordSalt = HashGenerator.createSalt()
        user.credentials.passwordHash = HashGenerator.generateHash(password, user.credentials.passwordSalt)

        DBObject mongoUser = JSON.parse(user.toJson())
        WriteResult result = users.insert(mongoUser)
        if (result.getN() == 1) {
            log.debug "Successfully created user"
        }
        return findByUsername(user.credentials.username).get()
    }

    void delete(User user) {
        users.remove(JSON.parse(user.toJson()))
    }

    User update(String username, User update) {
        DBObject mongoUpdate = JSON.parse(update.toJson())
        DBObject updatedUser = users.findAndModify(new BasicDBObject('credentials.username': username), mongoUpdate)
        return User.fromJson(JSON.serialize(updatedUser))
    }

    boolean userExists(String username) {
        return users.count(new BasicDBObject('credentials.username': username)) > 0
    }

}
