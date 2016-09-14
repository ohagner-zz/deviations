import com.ohagner.deviations.domain.User
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.registry.Registry

import static ratpack.groovy.Groovy.groovyHandler
import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

Logger log = LoggerFactory.getLogger("Ratpack")

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
                next(Registry.single(User, new User(firstName: username)))
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
                        render json(["message": "Retrieve user"])
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
