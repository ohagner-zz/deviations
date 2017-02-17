package com.ohagner.deviations.web.user.service

import com.google.common.base.Charsets
import com.google.inject.Inject
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import ratpack.exec.Promise
import ratpack.http.client.HttpClient
import ratpack.server.PublicAddress

@Slf4j
class DefaultUserService implements UserService {
    private static final def SUCCESS_RESPONSE_CODES = 200..299

    PublicAddress publicAddress
    HttpClient httpClient

    @Inject
    DefaultUserService(PublicAddress publicAddress, HttpClient httpClient) {
        this.publicAddress = publicAddress
        this.httpClient = httpClient
    }

    @Override
    Promise<Boolean> update(Map user) {
        httpClient.request(publicAddress.get("api/users/${user.username}")) { request ->
            request
                    .headers { it.add("Authorization", user.apiToken) }
                    .put()
                    .body.text(JsonOutput.toJson(user))
        }.mapError { t ->
            log.error("Failed to update user", t)
            return false
        }.map { response ->
            log.info("Update response : " + response.getBody()?.getText(Charsets.UTF_8))
            def success = SUCCESS_RESPONSE_CODES.contains(response.statusCode)
            log.info "Success: $success"
            return success
        }
    }

    @Override
    Promise<Boolean> create(Map user) {
        def requestBody = JsonOutput.toJson {
            firstName user.firstName
            lastName user.lastName
            emailAddress user.emailAddress
            credentials {
                username user.username
                password user.password
            }
            if(user.webhook) {
                webhook {
                    url user.webhook
                }
            }
            if(user.slackWebhook) {
                slackWebhook {
                    url user.slackWebhook
                }
            }
        }
        log.info "Sending ${JsonOutput.prettyPrint(requestBody)}"
        httpClient.request(publicAddress.get("api/users")) { request ->
            request
                    .post()
                    .body.text(requestBody)
        }.mapError { t ->
            log.error("Failed to create user", t)
            return false
        }.map { response ->
            log.info("Create user response: ${response.statusCode}" + response.getBody()?.getText(Charsets.UTF_8))
            def successValue = SUCCESS_RESPONSE_CODES.contains(response.statusCode)
            log.info "Success: $successValue"
            return successValue
        }
    }

}
