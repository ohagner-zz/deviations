package com.ohagner.deviations

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.util.JSON
import com.ohagner.deviations.domain.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.registry.Registry
import com.gmongo.*

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json



Logger log = LoggerFactory.getLogger("Ratpack")

GMongo mongo = new GMongo()
DB db = mongo.getDB('test')
DBCollection users = db.getCollection("users")
users.remove([:])
User user = new User(firstName: "Olle", lastName: "Hagner", emailAddress: "olle.hagner@gmail.com", username:"ohagner")
users.insert(JSON.parse(user.toJson()))



ratpack {
    bindings {
        module MarkupTemplateModule
    }

    handlers {
        all() {
            context.response.contentType("application/json")
            next()
        }
        prefix("deviations") {
            get("") {
                render json(["message": "Get all deviations"])
            }
            get(":transportType") {
                render json(["message": "Get all deviations for transport"])
            }
            get(":transportType/:lineNumber") {
                render json(["message": "Get all deviations for transport and lineNumber"])
            }
        }
        prefix(":username") {
            all {
                String username = pathTokens.username
                DBObject userObject = users.findOne(username:username)
                User callingUser = User.fromJson(JSON.serialize(userObject))
                callingUser ? next(Registry.single(User, callingUser)) : next()
            }
            path("") {
                context.byMethod {
                    post {
                        render json(["message": "Create user"])
                    }
                    delete {
                        render json(["message": "Delete user"])
                    }
                    get {
                        Optional<User> found = context.maybeGet(User)
                        if(found.isPresent()) {
                            render json(found.get())
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
            prefix("watches") {
                path("") {
                    context.byMethod {
                        post {
                            render json(["message": "Create watch"])
                        }
                        get {
                            render json(["message": "Retrieve all watches for user"])
                        }
                    }
                }
                path(":id") {
                    context.byMethod {
                        delete {
                            render json(["message": "Delete watch: " + pathTokens.id])
                        }
                        get {
                            render json(["message": "Retrieve watch for " + pathTokens.id])
                        }
                    }
                }
            }
            path("authenticate") {
                render json(["message": "Authenticating user"])
            }
            path("check") {
                render json(["message": "Perform check"])
            }
        }


        files { dir "public" }
    }
}
