import com.ohagner.deviations.api.ApiChain
import com.ohagner.deviations.api.common.JsonRenderingModule
import com.ohagner.deviations.api.deviation.endpoint.DeviationCheckHandler
import com.ohagner.deviations.api.deviation.endpoint.DeviationsChain
import com.ohagner.deviations.api.deviation.module.DeviationsModule
import com.ohagner.deviations.api.error.DefaultServerErrorHandler
import com.ohagner.deviations.api.journey.module.JourneyModule
import com.ohagner.deviations.api.journey.router.JourneyChain
import com.ohagner.deviations.api.notification.endpoint.SendNotificationHandler
import com.ohagner.deviations.api.notification.module.NotificationsModule
import com.ohagner.deviations.api.stop.module.StopsModule
import com.ohagner.deviations.api.user.domain.UserRenderer
import com.ohagner.deviations.api.user.endpoint.AdminAuthorizationHandler
import com.ohagner.deviations.api.user.endpoint.UserAuthenticationHandler
import com.ohagner.deviations.api.user.endpoint.UserAuthorizationHandler
import com.ohagner.deviations.api.user.router.AdminChain
import com.ohagner.deviations.api.user.service.security.DefaultAuthenticationService
import com.ohagner.deviations.api.watch.service.JobScheduler
import com.ohagner.deviations.api.watch.service.WatchProcessQueueingService
import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.modules.MessagingModule
import com.ohagner.deviations.modules.RepositoryModule
import com.ohagner.deviations.modules.ServiceModule
import com.ohagner.deviations.web.WebChain
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.dropwizard.metrics.DropwizardMetricsModule
import ratpack.dropwizard.metrics.MetricsWebsocketBroadcastHandler
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
        module(DropwizardMetricsModule) { config ->
            config.jvmMetrics(true)
        }
        module(MarkupTemplateModule) { config ->
            config.autoIndent = true
            config.autoNewLine = true
        }
        module SessionModule
        module RepositoryModule
        module DeviationsModule
        module StopsModule
        module MarkupTemplateModule
        module JsonRenderingModule
        module MessagingModule
        module NotificationsModule
        module ServiceModule
        module JourneyModule
        add new AdminChain()
        add new ApiChain()
        add new WebChain()
        bind DeviationsChain
        bind JourneyChain
        bind DefaultAuthenticationService
        bind UserRenderer
        bind JobScheduler
        bind WatchProcessQueueingService
        bind UserAuthorizationHandler
        bind AdminAuthorizationHandler
        bind SendNotificationHandler
        bind UserAuthenticationHandler
        bind DeviationCheckHandler
        bindInstance(ServerErrorHandler, new DefaultServerErrorHandler())
    }

    handlers {

        all RequestLogger.ncsa(log)
        get('metrics-report', new MetricsWebsocketBroadcastHandler())
        get(".well-known/acme-challenge/emAP7DINlj8L9lh9g5sC-V8qd8jrpz9UZZY5l0QPGRg") {
            render(file("public/certauth.txt"))
        }
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
