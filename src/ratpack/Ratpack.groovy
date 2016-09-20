import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.domain.User
import com.ohagner.deviations.handler.UserHandler
import com.ohagner.deviations.module.MongoModule
import com.ohagner.deviations.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.registry.Registry
import ratpack.server.BaseDir

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

Logger log = LoggerFactory.getLogger("Deviations-Main")

ratpack {

    serverConfig {
        baseDir(BaseDir.find())
        props("config/app.properties")
        env()
        require("/mongo", MongoConfig)
    }

    bindings {
        module MongoModule
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
        path("users") { UserRepository userRepository ->
            byMethod {
                get {
                    render json(userRepository.retrieveAll())
                }
                post {
                    request.getBody().then {
                        String request = it.text
                        log.info "Request: $request"
                        User requestUser = User.fromJson(request)

                        if (userRepository.userExists(requestUser.username)) {
                            response.status(400)
                            render json(["message": "User already exists"])
                        } else {
                            //validate user
                            User createdUser = userRepository.create(requestUser)
                            response.status(201)
                            render createdUser.toJson()
                        }
                    }
                }
            }
        }
        prefix("users/:username") {
            all { UserRepository userRepository ->
                String username = pathTokens.username
                User callingUser= userRepository.findByUsername(username)
                callingUser ? next(Registry.single(User, callingUser)) : next()
            }
            path("") {
                insert(new UserHandler())
            }
            all {
                Optional<User> user = context.maybeGet(User)
                if(!user.isPresent()) {
                    log.info "Not found"
                    response.status(404)
                    render json(["message": "Not found"])
                }
                next()
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
