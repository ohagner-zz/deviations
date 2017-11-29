package com.ohagner.deviations.api.stop.repository.trafiklab

import com.google.inject.Inject
import com.ohagner.deviations.api.stop.domain.Stop
import com.ohagner.deviations.api.stop.repository.StopRepository
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import ratpack.exec.Promise
import ratpack.http.HttpUrlBuilder
import ratpack.http.client.HttpClient
import ratpack.http.client.RequestSpec

@Slf4j
@TupleConstructor(force = true)
class HttpStopRepository implements StopRepository {

    private HttpClient httpClient
    private URI baseUri

    @Inject
    public HttpStopRepository(HttpClient httpClient, URI baseUri) {
        this.httpClient = httpClient
        this.baseUri = baseUri
    }

    @Override
    Promise<List<Stop>> findStops(String searchString) {
        log.info("Calling URI " + HttpUrlBuilder.base(baseUri).params(searchstring: searchString).build())
        httpClient.get(HttpUrlBuilder.base(baseUri).params(searchstring: searchString).build()) { RequestSpec requestSpec ->
            requestSpec.headers { headers ->
                headers.with {
                    add("Content-Type", "application/json")
                    add("Accept", "application/json")
                }
            }
        }
        .onError { t ->
            log.error("Failed to retrieve stop information", t)
        }
        .map { response ->
            log.info "Mapping response: ${response.dump()}"
            new StopTransformer().fromStopResponse(response.body.text)
        }
    }
}
