

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.WriteResult
import com.mongodb.util.JSON
import com.ohagner.deviations.domain.User
import com.ohagner.deviations.handler.UserHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.http.TypedData
import ratpack.registry.Registry
import com.gmongo.*

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json


Logger log = LoggerFactory.getLogger("Deviations-Main")

GMongo mongo = new GMongo()
DB db = mongo.getDB('test')
DBCollection users = db.getCollection("users")
users.remove([:])
User user = new User(firstName: "Olle", lastName: "Hagner", emailAddress: "olle.hagner@gmail.com", username: "ohagner")
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
        path("users") {
            byMethod {
                get {
                    render json(["message": "Get all users"])
                }
                post {
                    request.getBody().then {
                        String request = it.text
                        log.info "Request: $request"
                        User requestUser = User.fromJson(request)
                        boolean userExists = users.count(username: requestUser.username) > 0
                        if (userExists) {
                            response.status(400)
                            render json(["message": "User already exists"])
                        } else {
                            User newUser
                            //validate user
                            String userJson = requestUser.toJson()
                            log.info "UserJson: $userJson"
                            DBObject mongoUser = JSON.parse(userJson)
                            log.info "DBObject is null : ${mongoUser == null}"
                            WriteResult result = users.insert(mongoUser)
                            if (result.getN() == 1) {
                                log.info "Successfully created user"
                            }
                            render json(User.fromJson(JSON.serialize(users.findOne(username: requestUser.username))))
                        }
                    }
                }
            }
        }
        prefix("users/:username") {
            all {
                String username = pathTokens.username
                DBObject userObject = users.findOne(username: username)
                User callingUser = User.fromJson(JSON.serialize(userObject))
                callingUser ? next(Registry.single(User, callingUser)) : next()
            }
            path("") {
                insert(new UserHandler(users))
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
