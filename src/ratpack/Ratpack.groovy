import com.ohagner.deviations.domain.User
import groovy.json.JsonOutput
import ratpack.groovy.template.MarkupTemplateModule

import static ratpack.groovy.Groovy.groovyHandler
import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

ratpack {
  bindings {
    module MarkupTemplateModule
  }

  handlers {
    all() {
      context.response.contentType("application/json")
      next()
    }
//    get {
//      render groovyMarkupTemplate("index.gtpl", title: "My Ratpack App")
//    }
    path("user") {
      context.byMethod {
        post {
          User user = new User(firstName: "ture", lastName: "göte")
          render json(user)
        }
        delete {
          render json(["message":"Delete user"])
        }
      }
    }




    prefix("user/:username") {
      post("watches") { chain ->
        chain.context.render JsonOutput.toJson {
          message "Got called by user"
        }
      }
    }

    files { dir "public" }
  }
}
