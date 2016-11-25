package com.ohagner.deviations.web.service

import com.google.common.base.Charsets
import com.google.inject.Inject
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import ratpack.exec.Promise
import ratpack.http.client.HttpClient
import ratpack.server.PublicAddress

@Slf4j
class DefaultUserService implements UserService {

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
            def success = (200..299).contains(response.statusCode)
            log.info "Success: $success"
            return success
        }
    }

    @Override
    Promise<Boolean> create(Map user) {
        httpClient.request(publicAddress.get("api/users/${user.username}")) { request ->
            request
                    .post()
                    .body.text(user)
        }.mapError { t ->
            log.error("Failed to create user", t)
            return false
        }.then { response ->
            log.info(response.getBody()?.getText(Charsets.UTF_8))
            return (200..299).contains(response.statusCode)
        }
    }

}
