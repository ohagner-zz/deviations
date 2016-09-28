import com.ohagner.deviations.DeviationMatcher
import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.handler.UserHandler
import com.ohagner.deviations.modules.JsonRenderingModule
import com.ohagner.deviations.modules.MongoModule
import com.ohagner.deviations.modules.TrafikLabModule
import com.ohagner.deviations.notifications.EmailNotifier
import com.ohagner.deviations.notifications.LogNotifier
import com.ohagner.deviations.notifications.NotificationService
import com.ohagner.deviations.repository.DeviationRepository
import com.ohagner.deviations.repository.HttpDeviationRepository
import com.ohagner.deviations.repository.UserRepository
import com.ohagner.deviations.repository.WatchRepository
import com.ohagner.deviations.watch.WatchProcessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.exec.Blocking
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
        module TrafikLabModule
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
                log.info "Trying to retrieve user $username"
                Optional<User> callingUser = userRepository.findByUsername(username)
                callingUser.isPresent() ? next(Registry.single(User, callingUser.get())) : next()
            }
            path("") {
                insert(new UserHandler())
            }
            //Everything below to be moved to WatchHandler of some sort
            all {
                log.info "Looking in context for user"
                Optional<User> user = context.maybeGet(User)
                if (!user.isPresent()) {
                    log.info "User not found"
                    response.status(404)
                    render json(["message": "Not found"])
                } else {
                    next()
                }
            }
            prefix("watches") {
                path("") { WatchRepository watchRepository, User user ->
                    context.byMethod {
                        post {
                            request.body.then { body ->
                                log.info "Creating watch for user ${user.username}"
                                Watch watch = Watch.fromJson(body.text)
                                watch.username = user.username
                                render json(watchRepository.create(watch))
                            }
                        }
                        get {
                            render json(watchRepository.findByUsername(user.username))
                        }
                    }
                }
                path(":id") { WatchRepository watchRepository, User user ->
                    if(!pathTokens.id.isNumber()) {
                        response.status(400)
                        render json(["message": new String("Watch id ${pathTokens.id} is not numeric")])
                    }
                    long watchId = pathTokens.id as long
                    context.byMethod {
                        delete {
                            Optional<Watch> deletedWatch = watchRepository.delete(user.username, watchId)
                            if(deletedWatch.isPresent()) {
                                render json(deletedWatch.get())
                            } else {
                                response.status(500)
                                render json(["message": new String("Watch with id $watchId could not be deleted")])
                            }
                        }
                        get {
                            Optional<Watch> watch = watchRepository.findByUsernameAndId(user.username, watchId)
                            if(watch.isPresent()) {
                                render json(watch.get())
                            } else {
                                log.info "Watch not found"
                                response.status(404)
                                render json(["message": "Watch with id $watchId does not exist"])
                            }
                        }
                    }
                }
            }
            path("check") { WatchRepository watchRepository, UserRepository userRepository ->
                DeviationRepository deviationRepo = new HttpDeviationRepository()
                DeviationMatcher deviationMatcher = new DeviationMatcher(deviationRepo.retrieveAll())
                WatchProcessor processor = WatchProcessor.builder()
                        .notificationService(new NotificationService([new LogNotifier(), new EmailNotifier()], userRepository))
                        .deviationMatcher(deviationMatcher)
                        .watchRepository(watchRepository).build()
                log.info "Before blocking"
                Blocking.exec { processor.process() }
                log.info "After blocking"
                render json(["message": "Checking user"])
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
