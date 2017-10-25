package com.ohagner.deviations.api.journey.repository.trafiklab

import com.ohagner.deviations.api.journey.domain.Journey
import com.ohagner.deviations.api.journey.domain.JourneySearch
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import ratpack.exec.Promise
import ratpack.http.HttpUrlBuilder
import ratpack.http.client.HttpClient
import ratpack.http.client.RequestSpec

@Slf4j
@TupleConstructor(force = true)
class HttpJourneyRepository implements com.ohagner.deviations.api.journey.repository.JourneyRepository {

    private HttpClient httpClient
    private URI baseUri

    HttpJourneyRepository(HttpClient httpClient, URI baseUri) {
        this.httpClient = httpClient
        this.baseUri = baseUri
    }

    @Override
    Promise<List<Journey>> search(JourneySearch journeySearch) {
        def searchParams = [
                originId:journeySearch.origin.externalId,
                destId: journeySearch.destination.externalId,
                lang:"en"
        ]
        log.info("Calling URI " + HttpUrlBuilder.base(baseUri).params(searchParams).build())
        httpClient.get(HttpUrlBuilder.base(baseUri).params(searchstring: searchParams).build()) { RequestSpec requestSpec ->
            requestSpec.headers { headers ->
                headers.with {
                    add("Content-Type", "application/json")
                    add("Accept", "application/json")
                }
            }
        }
        .map { response ->
            return new JourneyTransformer().fromReseplanerareResponse(response.body.text)
        }

    }
}
