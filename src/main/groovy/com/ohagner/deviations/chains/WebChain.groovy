package com.ohagner.deviations.chains

import com.google.common.base.Charsets
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.parser.WatchFormParser
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import ratpack.form.Form
import ratpack.groovy.handling.GroovyChainAction
import ratpack.http.client.HttpClient
import ratpack.server.PublicAddress

import static groovy.json.JsonOutput.toJson
import static ratpack.groovy.Groovy.groovyMarkupTemplate

@Slf4j
class WebChain extends GroovyChainAction {
    @Override
    void execute() throws Exception {
        log.info "In webchain"

        get("") {
            log.info "GET"
            render groovyMarkupTemplate("index.gtpl", title: "Trafikbevakning", msg: request.queryParams.msg ?: "")
        }
        path("login") {
            byMethod {
                get {
                    render groovyMarkupTemplate("login.gtpl",
                            title: "Login",
                            buttonText: "Login",
                            msg: request.queryParams.msg ?: "")
                }
                post {
                    parse(Form).then { form ->
                        log.info "Form as json ${toJson(form)}"
                        redirect "/?msg=Logged+in"
                    }
                }
            }
        }
        prefix("user") {
            path("create") { HttpClient httpClient, PublicAddress publicAddress ->
                byMethod {
                    get {
                        log.info "Handling get"
                        render groovyMarkupTemplate("user-create.gtpl",
                                title: "Create user",
                                username: "Ture",
                                buttonText: "Create",
                                disabledFields: [],
                                msg: request.queryParams.msg ?: "")

                    }
                    post {
                        log.info "Saving user"
                        parse(Form).then { form ->
                            log.info "Form as json ${toJson(form)}"
                            def createUserRequest = JsonOutput.toJson {
                                firstName form.firstName
                                lastName form.lastName
                                emailAddress form.emailAddress
                                credentials {
                                    username form.username
                                    password form.password
                                }
                            }
                            httpClient.post(publicAddress.get("api/users")) { request ->
                                request.body.text(createUserRequest)
                            }
                            //Hantera felaktiga
                            .map { response ->
                                log.info("POST /api/users ${response.statusCode}")
                                if(!(200..299).contains(response.statusCode)){
                                    throw new Exception("Failed to create user")
                                }
                            }.onError { t ->
                                log.error("Something went wrong", t)
                                redirect "/?msg=Failed+to+save+user"
                            }.then {
                                redirect "/?msg=User+created"
                            }
                        }
                    }
                }
            }
            path("update") {
                byMethod {
                    get {
                        log.info "Retrieve user from session here"
                        render groovyMarkupTemplate("user-update.gtpl",
                                title: "Update user",
                                username: "Ture",
                                firstName: "Ture",
                                lastName: "Göte",
                                emailAddress: "test@test.com",
                                buttonText: "Create",
                                disabledFields: ["username"])
                    }
                    post { HttpClient httpClient, PublicAddress publicAddress ->
                        log.info "Updating user"
                        parse(Form).then { form ->
                            log.info "Form as json ${toJson(form)}"
                            def updateUserRequest = JsonOutput.toJson {
                                firstName form.firstName
                                lastName form.lastName
                                emailAddress form.emailAddress
                            }
                            //This should be PATCH
                            httpClient.post(publicAddress.get("api/users")) { request ->
                                request.body.text(updateUserRequest)
                            }
                            .map { response ->
                                log.info(response.getBody().getText(Charsets.UTF_8))
                            }.mapError { t ->
                                log.error("Something went wrong", t)
                                redirect "user/create?msg=Failed+to+update+user"
                            }.then {
                                redirect "/?msg=User+updated"
                            }
                        }
                    }
                }
            }
        }
        prefix("watch/create") {
            path("weekly") { HttpClient httpClient, PublicAddress publicAddress ->
                byMethod {
                    post {
                        //Hämta användare här också
                        log.info "Creating watch"
                        parse(Form).then { form ->
                            log.info "Form as json ${toJson(form)}"
                            def watchRequest = JsonOutput.toJson {
                                name form.name
                                notifyMaxHoursBefore form.notifyMaxHoursBefore
                                schedule {
                                    weekDays WatchFormParser.getWeekdays(form)
                                    timeOfEvent form.timeOfEvent
                                    type "WEEKLY"
                                }
                                transports(WatchFormParser.getTransports(form))
                                notifyBy(WatchFormParser.getNotificationTypes(form))
                            }

                            httpClient.post(publicAddress.get("api/users/ohagner/watches")) { request ->
                                request.body.text(watchRequest)
                            }.map { response ->
                                if(!response.statusCode ==~ /2\d\d/) {
                                    throw new Exception("Failed")
                                }
                                log.info(response.getBody().getText(Charsets.UTF_8))
                            }.mapError { t ->
                                log.error("Something went wrong", t)
                                redirect "/watch/create?msg=Failed+to+create+watch"
                            }.then {
                                redirect "/watches?msg=Watch+created"
                            }
                        }
                    }
                    get {
                        render groovyMarkupTemplate("weekly-watch-create.gtpl",
                                title: "Skapa bevakning",
                                msg: request.queryParams.msg ?: "")
                    }
                }
            }
            path("single") { HttpClient httpClient, PublicAddress publicAddress ->
                byMethod {
                    post {
                        //Hämta användare här också
                        log.info "Creating watch"
                        parse(Form).then { form ->
                            log.info "Form as json ${toJson(form)}"

                            def watchRequest = JsonOutput.toJson {
                                name form.name
                                notifyMaxHoursBefore form.notifyMaxHoursBefore
                                schedule {
                                    dateOfEvent form.dateOfEvent
                                    timeOfEvent form.timeOfEvent
                                    type "SINGLE"
                                }
                                transports(WatchFormParser.getTransports(form))
                                notifyBy(WatchFormParser.getNotificationTypes(form))
                            }
                            log.info "Created json: ${JsonOutput.prettyPrint(watchRequest)}"
                            httpClient.post(publicAddress.get("api/users/ohagner/watches")) { request ->
                                request.body.text(watchRequest)
                            }
                            .map { response ->
                                log.info(response.getBody().getText(Charsets.UTF_8))
                            }.mapError { t ->
                                log.error("Something went wrong", t)
                                redirect "/watch/create/single?msg=Failed+to+create+watch"
                            }.then {
                                redirect "/watches?msg=Watch+created"
                            }
                        }
                    }
                    get {
                        render groovyMarkupTemplate("single-watch-create.gtpl",
                                msg: request.queryParams.msg ?: "",
                                title: "Skapa bevakning")
                    }
                }
            }

        }
        prefix("watches") {
            log.info "Matched watches"
            //Hämta användare här
            get("") { HttpClient httpClient, PublicAddress publicAddress ->
                httpClient.get(publicAddress.get("api/users/ohagner/watches")) { request ->
                    request.headers.add("securityToken", "user.securityToken")
                }.onError { t ->
                    log.warn("Failed to retrieve watches")
                    redirect "/?msg=Failed+to+retrieve+watches"
                }.then { response ->
                    log.info "Response: ${response?.body?.text}"
                    def json = new JsonSlurper().parseText(response.body.text)
                    def weeklyWatches = json.findAll { it.schedule.type == 'WEEKLY'}
                    def singleOccurrenceWatches = json.findAll { it.schedule.type == 'SINGLE'}
                    //Don't use domain class here
//                    List<Watch> watches = json.collect { Watch.fromJson(JsonOutput.toJson(it)) }
                    log.info "weekly watches size: ${weeklyWatches.size()}"
                    log.info("Content: " + weeklyWatches.first().getClass().toString())
                    render groovyMarkupTemplate("list-watches.gtpl",
                            title: "Bevakningslista",
                            username: "",
                            msg: request.queryParams.msg ?: "",
                            weeklyWatches: weeklyWatches,
                            singleOccurrenceWatches: singleOccurrenceWatches)
                }

            }
            post("delete/:id") { HttpClient httpClient, PublicAddress publicAddress ->

                String id = pathTokens.id
                log.info "Deleting watch $id"
                httpClient.request(publicAddress.get("api/users/ohagner/watches/$id")) { request ->
                    request.headers.add("Content-Type", "application/json")
                    request.delete()
                }.onError { throwable ->
                    log.warn("Failed to delete watch", throwable)
                    render groovyMarkupTemplate("list-watches.gtpl",
                            title: "Bevakningslista",
                            username: "",
                            watches: [],
                            msg: "Failed to delete watch")
                }.then { response ->
                    log.info "Delete watch response: ${response?.body?.text}"
                    redirect "/watches?msg=Deleted+watch"
                }

            }

        }
    }
}
