import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.handler.UserHandler
import com.ohagner.deviations.modules.JsonRenderingModule
import com.ohagner.deviations.modules.MongoModule
import com.ohagner.deviations.repository.UserRepository
import com.ohagner.deviations.repository.WatchRepository
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
        module JsonRenderingModule
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
                User callingUser = userRepository.findByUsername(username)
                callingUser ? next(Registry.single(User, callingUser)) : next()
            }
            path("") {
                insert(new UserHandler())
            }
            //Everything below to be moved to WatchHandler of some sort
            all {
                Optional<User> user = context.maybeGet(User)
                if (!user.isPresent()) {
                    log.info "Not found"
                    response.status(404)
                    render json(["message": "Not found"])
                }
                next()
            }
            prefix("watches") {
                path("") { WatchRepository watchRepository, User user ->
                    context.byMethod {
                        post {
                            request.body.then { body ->
                                log.info "Creating watch for user ${user.username}"
                                Watch watch = Watch.fromJson(body.text)
                                if(watchRepository.exists(user.username, watch.name)) {
                                    response.status(400)
                                    String message = "Unable to create watch ${watch.name}, it already exists"
                                    render json(["message": message])
                                } else {
                                    watch.username = user.username
                                    render json(watchRepository.create(watch))
                                }
                            }
                        }
                        get {
                            render json(watchRepository.findByUsername(user.username))
                        }
                    }
                }
                path(":name") { WatchRepository watchRepository, User user ->
                    context.byMethod {
                        delete {
                            watchRepository.delete(user.username, pathTokens.name)
                            render json(["message": "Deleted watch: " + pathTokens.name])
                        }
                        get {
                            render json(watchRepository.findByUsernameAndName(user.username, pathTokens.name))
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
