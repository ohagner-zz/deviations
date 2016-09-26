package com.ohagner.deviations.repository

import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.WriteResult
import com.mongodb.util.JSON
import com.ohagner.deviations.domain.User
import groovy.util.logging.Slf4j

@Slf4j
class UserRepository {

    DBCollection users

    UserRepository(DBCollection collection) {
        this.users = collection
        log.info "UserRepository initialized"
    }

    User findByUsername(String username) {
        log.debug "Retrieving data for user $username"
        DBObject userObject = users.findOne(new BasicDBObject(username: username))
        return User.fromJson(JSON.serialize(userObject))
    }

    List<User> retrieveAll() {
        DBCursor cursor = users.find()
        List<User> users = cursor.iterator().collect { User.fromJson(JSON.serialize(it)) }
        return users
    }

    User create(User user) {
        DBObject mongoUser = JSON.parse(user.toJson())
        WriteResult result = users.insert(mongoUser)
        if (result.getN() == 1) {
            log.info "Successfully created user"
        }
        return findByUsername(user.username)
    }

    void delete(User user) {
        users.remove(JSON.parse(user.toJson()))
    }

    User update(String username, User update) {
        DBObject mongoUpdate = JSON.parse(update.toJson())
        DBObject updatedUser = users.findAndModify(new BasicDBObject(username:username), mongoUpdate)
        return User.fromJson(JSON.serialize(updatedUser))
    }

    boolean userExists(String username) {
        return users.count(new BasicDBObject(username: username)) > 0
    }

}
