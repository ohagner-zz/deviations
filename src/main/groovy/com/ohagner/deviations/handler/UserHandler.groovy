package com.ohagner.deviations.handler

import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.WriteResult
import com.mongodb.util.JSON
import com.ohagner.deviations.domain.User
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler
import ratpack.http.TypedData

import static ratpack.jackson.Jackson.json

@Slf4j
class UserHandler extends GroovyHandler {

    DBCollection users

    public UserHandler(DBCollection users) {
        this.users = users
    }

    @Override
    void handle(GroovyContext ctx) throws Exception {
        ctx.with {
            byMethod {
                Optional<User> user = context.maybeGet(User)
                post {

                }
                delete {
                    if (user.isPresent()) {
                        log.info "Deleting user"
                        users.remove(JSON.parse(user.get().toJson()))
                        render json(user.get())
                    } else {
                        log.info "NOT FOUND"
                        clientError(404)
                    }

                    render json(["message": "Delete user"])
                }
                get {
                    if (user.isPresent()) {
                        render json(user.get())
                    } else {
                        log.info "NOT FOUND"
                        clientError(404)
                    }
                }
                put {
                    render json(["message": "Update user"])
                }

            }
        }
    }
}
