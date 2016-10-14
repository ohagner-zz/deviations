package com.ohagner.deviations.testutils

import wslite.http.HTTPClient
import wslite.http.HTTPRequest
import wslite.http.HTTPResponse

class MockHTTPClient extends HTTPClient {

    //HTTPResponse response
    HTTPRequest request

    Queue<HTTPResponse> responses

    @Override
    HTTPResponse execute(HTTPRequest request) {
        this.request = request
        def response = responses.poll()
        response.url = request.url
        response.date = new Date()
        return response
    }
}