package com.ohagner.deviations.api.user.repository

import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.WriteResult
import com.mongodb.util.JSON
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.service.security.HashGenerator
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

    Promise<User> findByUsername(String username) {
        log.debug "Retrieving data for user $username"

        Promise.sync {
            DBObject userObject = users.findOne(new BasicDBObject('credentials.username': username))
            return userObject ? User.fromJson(JSON.serialize(userObject)) : null
        }
    }

    Promise<User> findByApiToken(String apiToken) {
        if(!apiToken) {
            return Promise.value(null)
        }
        Blocking.get {
            log.info "Looking for user with apiToken $apiToken"
            DBObject userObject = users.findOne(new BasicDBObject('credentials.apiToken.value': apiToken))
            log.info "User found: ${userObject != null}"
            return userObject ? User.fromJson(JSON.serialize(userObject)) : null
        }
    }

    Promise<List<User>> retrieveAll() {
        DBCursor cursor = users.find()
        List<User> users = cursor.iterator().collect { User.fromJson(JSON.serialize(it)) }
        return Promise.value(users)
    }

    Promise<User> create(User user, String password) {
        user.credentials.passwordSalt = HashGenerator.createSalt()
        user.credentials.passwordHash = HashGenerator.generateHash(password, user.credentials.passwordSalt)

        DBObject mongoUser = JSON.parse(user.toJson())
        WriteResult result = users.insert(mongoUser)
        if (result.getN() == 1) {
            log.debug "Successfully created user"
        }
        return findByUsername(user.credentials.username)
    }

    void delete(User user) {
        users.remove(JSON.parse(user.toJson()))
    }

    Promise<User> update(String username, User update) {
        Blocking.get {
            DBObject mongoUpdate = JSON.parse(update.toJson())
            DBObject updatedUser = users.findAndModify(new BasicDBObject('credentials.username': username), mongoUpdate)
            return User.fromJson(JSON.serialize(updatedUser))
        }.flatMap { user ->
            findByUsername(user.credentials.username)
        }
    }

    Promise<Boolean> userExists(String username) {
        return Promise.value(users.count(new BasicDBObject('credentials.username': username)) > 0)
    }

}
