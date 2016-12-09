import com.ohagner.deviations.api.deviation.DeviationsModule
import com.ohagner.deviations.api.notification.NotificationsModule
import com.ohagner.deviations.api.user.AdminChain
import com.ohagner.deviations.api.ApiChain
import com.ohagner.deviations.api.deviation.DeviationsChain
import com.ohagner.deviations.web.WebChain
import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.api.user.UserRenderer
import com.ohagner.deviations.api.error.DefaultServerErrorHandler
import com.ohagner.deviations.api.user.AdminAuthorizationHandler
import com.ohagner.deviations.api.user.UserAuthenticationHandler
import com.ohagner.deviations.api.user.UserAuthorizationHandler
import com.ohagner.deviations.api.notification.SendNotificationHandler
import com.ohagner.deviations.modules.*
import com.ohagner.deviations.api.watch.JobScheduler
import com.ohagner.deviations.api.user.security.DefaultAuthenticationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.error.ServerErrorHandler
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.handling.RequestLogger
import ratpack.session.SessionModule

import static ratpack.groovy.Groovy.ratpack

Logger log = LoggerFactory.getLogger("Deviations-Main")

ratpack {

    serverConfig {
        props("config/app.properties")
        env()
        require("/mongo", MongoConfig)
    }

    bindings {
        module(MarkupTemplateModule) { config ->
            config.autoIndent = true
            config.autoNewLine = true
        }
        module SessionModule
        module RepositoryModule
        module DeviationsModule
        module MarkupTemplateModule
        module JsonRenderingModule
        module MessagingModule
        module NotificationsModule
        module ServiceModule
        add new AdminChain()
        add new ApiChain()
        add new WebChain()
        bind DeviationsChain
        bind DefaultAuthenticationService
        bind UserRenderer
        bind JobScheduler
        bind UserAuthorizationHandler
        bind AdminAuthorizationHandler
        bind SendNotificationHandler
        bind UserAuthenticationHandler
        bindInstance(ServerErrorHandler, new DefaultServerErrorHandler())
    }

    handlers {

        all RequestLogger.ncsa(log)
        prefix("admin") {
            all() {
                context.response.contentType("application/json")
                next()
            }
            all(AdminAuthorizationHandler)
            insert(AdminChain)
        }
        prefix("api") {
            all() {
                context.response.contentType("application/json")
                next()
            }
            insert(ApiChain)
        }
        insert(WebChain)
        files { dir "public" }
    }
}
