import com.ohagner.deviations.DeviationMatcher
import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.domain.User
import com.ohagner.deviations.handler.UserHandler
import com.ohagner.deviations.handler.WatchChain
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
            //Everything below to be moved to WatchChain of some sort
            prefix("watches") {
                insert(new WatchChain())
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
        }

        files { dir "public" }
    }
}
