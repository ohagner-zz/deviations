import com.ohagner.deviations.DeviationFilter
import com.ohagner.deviations.chains.AdminChain
import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.domain.User
import com.ohagner.deviations.handlers.UserHandler
import com.ohagner.deviations.handlers.WatchChain
import com.ohagner.deviations.modules.DeviationsModule
import com.ohagner.deviations.modules.JsonRenderingModule
import com.ohagner.deviations.modules.MessagingModule
import com.ohagner.deviations.modules.NotificationsModule
import com.ohagner.deviations.modules.RepositoryModule
import com.ohagner.deviations.repository.DeviationRepository
import com.ohagner.deviations.repository.UserRepository
import com.ohagner.deviations.scheduler.JobScheduler
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
        module RepositoryModule
        module DeviationsModule
        module MarkupTemplateModule
        module JsonRenderingModule
        module MessagingModule
        module NotificationsModule
        add new AdminChain()
        bind JobScheduler
    }

    handlers {
        all() {
            context.response.contentType("application/json")
            next()
        }
        prefix("admin") {
            insert(AdminChain)
        }
        prefix("deviations") {
            get("") {
                DeviationRepository deviationRepo = context.get(DeviationRepository)
                render json(DeviationFilter.apply(deviationRepo.retrieveAll()))
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
            //A user should be able to trigger a watch check
            /*path("check") { WatchRepository watchRepository, UserRepository userRepository ->
                DeviationRepository deviationRepo = new HttpDeviationRepository()
                DeviationMatcher deviationMatcher = new DeviationMatcher(deviationRepo.retrieveAll())
                WatchProcessor processor = WatchProcessor.builder()
                        .deviationMatcher(deviationMatcher)
                        .deviationsApiClient(watchRepository).build()
                log.info "Before blocking"
                Blocking.exec { processor.process() }
                log.info "After blocking"
                render json(["message": "Checking user"])
            }*/
        }

        files { dir "public" }
    }
}
