package com.ohagner.deviations.chains

import com.google.common.base.Charsets
import com.ohagner.deviations.config.Constants
import com.ohagner.deviations.domain.web.WebUser
import com.ohagner.deviations.parser.WatchFormParser
import com.ohagner.deviations.web.service.UserService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import ratpack.exec.Promise
import ratpack.form.Form
import ratpack.groovy.handling.GroovyChainAction
import ratpack.http.client.HttpClient
import ratpack.http.client.ReceivedResponse
import ratpack.registry.Registry
import ratpack.server.PublicAddress
import ratpack.session.Session

import static com.ohagner.deviations.config.Constants.Headers.USER_TOKEN
import static groovy.json.JsonOutput.toJson
import static ratpack.groovy.Groovy.groovyMarkupTemplate

@Slf4j
class WebChain extends GroovyChainAction {
    @Override
    void execute() throws Exception {

        get("") { Session session ->
            log.info "GET with sessionId: ${request.oneCookie("JSESSIONID")}"
            session.get(Constants.Session.LOGGED_IN_USER).map { optUser ->
                optUser.orElse([:])
            }.then { user ->
                render groovyMarkupTemplate("index.gtpl", title: "Trafikbevakning", msg: request.queryParams.msg ?: "", user: user)
            }

        }
        path("login") { HttpClient httpClient, PublicAddress publicAddress, Session session ->
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
                        def loginRequest = JsonOutput.toJson {
                            username form.username
                            password form.password
                        }
                        httpClient.post(publicAddress.get("api/authenticate")) { request ->
                            request.body.text(loginRequest)
                        }
                        .map { ReceivedResponse response ->
                            log.info("Response status: ${response.statusCode}, body: ${response.getBody().getText(Charsets.UTF_8)}")
                            response
                        }.mapError { t ->
                            log.error("Something went wrong", t)
                            redirect "login?msg=Failed+to+login"
                        }.then { ReceivedResponse response ->
                            if (response.statusCode ==~ /2\d\d/) {
                                def json = new JsonSlurper().parseText(response.body.getText(Charsets.UTF_8))
                                log.info "Setting user ${json.credentials.username} in session"
                                def user = [username : json.credentials.username,
                                            firstName: json.firstName, lastName: json.lastName, emailAddress: json.emailAddress, apiToken: json.credentials.apiToken.value]
                                session.set(Constants.Session.LOGGED_IN_USER, user).promise().then {
                                    redirect "/?msg=Logged+in"
                                }
                            } else {
                                redirect "login?msg=Failed+to+login"
                            }

                        }

                    }
                }
            }
        }
        path("logout") { Session session ->
            session.remove(Constants.Session.LOGGED_IN_USER).promise()
                    .then {
                redirect "/?msg=Logged out"
            }
        }
        prefix("user") {
            path("create") { UserService userService ->
                byMethod {
                    get {
                        log.info "Handling get"
                        render groovyMarkupTemplate("user-create.gtpl",
                                title: "Registrera ny användare",
                                buttonText: "Skapa",
                                disabledFields: [],
                                msg: request.queryParams.msg ?: "")

                    }
                    post {
                        log.info "Saving user"
                        parse(Form).flatMap { form ->
                            userService.create(form)
                        }.then { def success ->
                            log.info "In Webchain, success $success"
                            if (success) {
                                redirect "/?msg=Användare skapad"
                            } else {
                                redirect "/?msg=Kunde+inte+spara+användare"
                            }
                        }

                    }
                }
            }
            all { Session session ->
                log.info "After user create all"
                session.require(Constants.Session.LOGGED_IN_USER)
                        .onNull {
                    log.info "User does not exist, no update"
                    redirect "/?msg=Logga in först"
                }
                .then { user ->
                    log.info "User: ${user.dump()}"
                    WebUser webuser = WebUser.builder()
                            .firstName(user.firstName)
                            .lastName(user.lastName)
                            .emailAddress(user.emailAddress)
                            .username(user.username)
                            .apiToken(user.apiToken).build()
                    log.info "User exists, moving on"
                    next(Registry.single(WebUser, webuser))
                }
            }
            path("update") { Session session, UserService userService ->
                log.info "In path update"
                byMethod {
                    get {
                        log.info "Retrieve user from session here"
                        session.require(Constants.Session.LOGGED_IN_USER).then { loggedInUser ->
                            render groovyMarkupTemplate("user-update.gtpl",
                                    msg: request.queryParams.msg ?: "",
                                    title: "Ändra användaruppgifter",
                                    user: loggedInUser,
                                    username: loggedInUser.username,
                                    firstName: loggedInUser.firstName,
                                    lastName: loggedInUser.lastName,
                                    emailAddress: loggedInUser.emailAddress,
                                    buttonText: "Uppdatera",
                                    disabledFields: ["username"])
                        }
                    }
                    post {
                        log.info "Updating user"

                        parse(Form).right(getLoggedInUser(session))
                                .map { pair ->
                            def formUser = pair.left
                            def loggedInUser = pair.right
                            [username    : loggedInUser.username,
                             apiToken    : loggedInUser.apiToken,
                             firstName   : formUser.firstName,
                             lastName    : formUser.lastName,
                             emailAddress: formUser.emailAddress]
                        }.then { user ->
                            userService.update(user).then { success ->
                                if (success) {
                                    redirect "/?msg=Användare+uppdaterad"
                                } else {
                                    redirect "/user/update?msg=Användare+kunde+inte+uppdateras"
                                }
                            }
                        }
                    }
                }
            }
        }


        prefix("watch/create") {
            all { Session session ->
                log.info "After user create all"
                session.require(Constants.Session.LOGGED_IN_USER)
                        .onNull {
                    log.info "User does not exist, no update"
                    redirect "/?msg=Logga+in+först"
                }
                .then { user ->
                    log.info "User: ${user.dump()}"
                    WebUser webuser = WebUser.builder()
                            .firstName(user.firstName)
                            .lastName(user.lastName)
                            .emailAddress(user.emailAddress)
                            .username(user.username)
                            .apiToken(user.apiToken).build()
                    log.info "User exists, moving on"
                    next(Registry.single(WebUser, webuser))
                }
            }
            path("weekly") { HttpClient httpClient, PublicAddress publicAddress, WebUser webUser ->
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

                            httpClient.post(publicAddress.get("api/users/${webUser.username}/watches")) { request ->
                                request.headers.add(USER_TOKEN, webUser.apiToken)
                                request.body.text(watchRequest)
                            }.map { response ->
                                if (!response.statusCode ==~ /2\d\d/) {
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
                                user: [username: webUser.username],
                                msg: request.queryParams.msg ?: "")
                    }
                }
            }
            path("single") { HttpClient httpClient, PublicAddress publicAddress, WebUser webUser ->
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
                            httpClient.post(publicAddress.get("api/users/${webUser.username}/watches")) { request ->
                                request.headers.add(USER_TOKEN, webUser.apiToken)
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
                                user: [username: webUser.username],
                                title: "Skapa bevakning")
                    }
                }
            }

        }
        prefix("watches") {
            log.info "Matched watches"
            all { Session session ->
                log.info "After user create all"
                session.require(Constants.Session.LOGGED_IN_USER)
                        .onNull {
                    log.info "User does not exist, no update"
                    redirect "/?msg=Logga+in+först"
                }
                .then { user ->
                    log.info "User: ${user.dump()}"
                    WebUser webuser = WebUser.builder()
                            .firstName(user.firstName)
                            .lastName(user.lastName)
                            .emailAddress(user.emailAddress)
                            .username(user.username)
                            .apiToken(user.apiToken).build()
                    log.info "User exists, moving on"
                    next(Registry.single(WebUser, webuser))
                }
            }
            //Hämta användare här
            get("") { HttpClient httpClient, PublicAddress publicAddress, WebUser webUser ->
                log.info "Retrieving watches with apiToken ${webUser.apiToken}"
                httpClient.get(publicAddress.get("api/users/${webUser.username}/watches")) { request ->
                    request.headers.add(USER_TOKEN, webUser.apiToken)
                }.onError { t ->
                    log.warn("Failed to retrieve watches")
                    redirect "/?msg=Failed+to+retrieve+watches"
                }.then { response ->
                    log.info "Response: ${response?.body?.text}"
                    def json = new JsonSlurper().parseText(response.body.text)
                    def weeklyWatches = json.findAll { it.schedule.type == 'WEEKLY' }
                    def singleOccurrenceWatches = json.findAll { it.schedule.type == 'SINGLE' }
                    log.info "weekly watches size: ${weeklyWatches.size()}"
                    render groovyMarkupTemplate("list-watches.gtpl",
                            title: "Bevakningslista",
                            username: "",
                            user: [username: webUser.username],
                            msg: request.queryParams.msg ?: "",
                            weeklyWatches: weeklyWatches,
                            singleOccurrenceWatches: singleOccurrenceWatches)
                }

            }
            post("delete/:id") { HttpClient httpClient, PublicAddress publicAddress, WebUser webUser ->

                String id = pathTokens.id
                log.info "Deleting watch $id"
                httpClient.request(publicAddress.get("api/users/${webUser.username}/watches/$id")) { request ->
                    request.headers.add("Content-Type", "application/json")
                    request.headers.add(USER_TOKEN, webUser.apiToken)
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

    Promise getLoggedInUser(Session session) {
        session.require(Constants.Session.LOGGED_IN_USER)
    }

}
