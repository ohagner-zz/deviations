import com.ohagner.deviations.chains.AdminChain
import com.ohagner.deviations.chains.WebChain
import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.handlers.ApiChain
import com.ohagner.deviations.modules.*
import com.ohagner.deviations.scheduler.JobScheduler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.handling.RequestLogger
import ratpack.server.BaseDir
import ratpack.session.SessionModule

import static ratpack.groovy.Groovy.groovyMarkupTemplate
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
        add new AdminChain()
        add new ApiChain()
        add new WebChain()
        bind JobScheduler
    }

    handlers {

        all RequestLogger.ncsa(log)
        prefix("admin") {
            all() {
                context.response.contentType("application/json")
                next()
            }
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
