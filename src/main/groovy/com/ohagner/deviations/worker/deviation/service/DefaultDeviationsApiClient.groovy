package com.ohagner.deviations.worker.deviation.service

import com.ohagner.deviations.config.AppConfig
import com.ohagner.deviations.config.Constants
import com.ohagner.deviations.api.deviation.domain.Deviation
import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.notification.domain.Notification
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import wslite.rest.ContentType
import wslite.rest.RESTClient
import wslite.rest.Response

import java.time.LocalDate

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

@Slf4j
class DefaultDeviationsApiClient implements DeviationsApiClient {

    private RESTClient client

    private static String apiToken
    private static LocalDate apiTokenExpirationDate
    private static final String ADMIN_USERNAME = AppConfig.envOrDefault(Constants.Admin.USERNAME, "admin")
    private static final String ADMIN_PASSWORD = AppConfig.envOrDefault(Constants.Admin.PASSWORD, "admin")


    boolean sendNotifications(Watch watch, Set<Deviation> matchingDeviations) {
        try {
            updateCredentialsIfNeeded()
            Notification notification = Notification.fromDeviations(matchingDeviations, watch.notifyBy)
            log.debug "Sending notification ${notification.toString()}"
            Response notificationResponse = client.post(path: "/admin/users/${watch.username}/notification", headers: [Authorization: apiToken]) {
                type ContentType.JSON
                text notification.toJson()
            }
            log.debug "Notification response status ${notificationResponse.statusCode}"
            return notificationResponse.statusCode ==~ /2\d\d/
        } catch (Exception e) {
            log.error("Failed to send notification", e)
            return false
        }
    }

    boolean update(Watch watch) {
        try {
            updateCredentialsIfNeeded()
            Response updateResponse = client.put(path: "/admin/watches/${watch.id}", headers: [Authorization: apiToken]) {
                type ContentType.JSON
                text watch.toJson()
            }
            log.debug "Update watch response status ${updateResponse.statusCode}"
            return updateResponse.statusCode ==~ /2\d\d/
        } catch (Exception e) {
            log.error("Failed to send notification", e)
            return false
        }

    }

    private void updateCredentialsIfNeeded() {
        log.info "Checking to see if credentials should be updated"
        if (!tokenIsValid()) {
            log.info "Updating credentials"
            try {
                Response response = client.post(path: "/api/authenticate") {
                    type ContentType.JSON
                    text JsonOutput.toJson([username: ADMIN_USERNAME, password: ADMIN_PASSWORD])
                }
                def user = new JsonSlurper().parse(response.data, "UTF-8")
                apiToken = user.credentials.apiToken.value
                apiTokenExpirationDate = LocalDate.parse(user.credentials.apiToken.expirationDate, ISO_LOCAL_DATE)

            } catch (Exception e) {
                log.error("Failed to authenticate admin user", e)
                throw e
            }
        }
    }

    private boolean tokenIsValid() {
        return apiToken && apiTokenExpirationDate && apiTokenExpirationDate.isAfter(LocalDate.now(Constants.ZONE_ID))
    }
}
