import com.ohagner.deviations.chains.AdminChain
import com.ohagner.deviations.config.MongoConfig
import com.ohagner.deviations.handlers.ApiChain
import com.ohagner.deviations.modules.*
import com.ohagner.deviations.scheduler.JobScheduler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.handling.RequestLogger
import ratpack.server.BaseDir

import static ratpack.groovy.Groovy.ratpack

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
        add new ApiChain()
        bind JobScheduler
    }

    handlers {
        all() {
            context.response.contentType("application/json")
            next()
        }
        all RequestLogger.ncsa(log)
        prefix("admin") {
            insert(AdminChain)
        }
        prefix("api") {
            insert(ApiChain)
        }
        files { dir "public" }
    }
}
