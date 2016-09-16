package com.ohagner.deviations.repository

import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.WriteResult
import com.mongodb.util.JSON
import com.ohagner.deviations.domain.User
import groovy.util.logging.Slf4j

@Slf4j
final class UserRepository {

    DBCollection users

    UserRepository(DBCollection collection) {
        users = collection
    }

    User findByUsername(String username) {
        DBObject userObject = users.findOne(username: username)
        return User.fromJson(JSON.serialize(userObject))
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

    boolean userExists(String username) {
        return users.count(username: username) > 0
    }

}
