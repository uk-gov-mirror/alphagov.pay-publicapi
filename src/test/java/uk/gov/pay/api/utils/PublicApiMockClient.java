package uk.gov.pay.api.utils;

import org.mockserver.client.server.MockServerClient;

import javax.ws.rs.core.HttpHeaders;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class PublicApiMockClient {

    private final MockServerClient mockClient;
    private final String baseUrl;

    public PublicApiMockClient(int mockServerPort, String baseUrl) {
        this.mockClient = new MockServerClient("localhost", mockServerPort);
        this.baseUrl = baseUrl;
    }

    public void respondUnauthorised() {
        mockClient.when(request()
                .withMethod(GET)
                .withHeader(ACCEPT, APPLICATION_JSON))
                .respond(response().withStatusCode(401));

    }

    public void mapBearerTokenToAccountId(String bearerToken, String gatewayAccountId) {
        mockClient.when(request()
                .withPath("/v1/auth")
                .withMethod(GET)
                .withHeader(ACCEPT, APPLICATION_JSON)
                .withHeader(AUTHORIZATION, "Bearer " + bearerToken))
                .respond(response().withStatusCode(200).withBody("{\"account_id\" : \"" + gatewayAccountId + "\"}").withHeader(CONTENT_TYPE, APPLICATION_JSON));

    }

    public void respondWithError() {
        mockClient.when(request()
                .withMethod(GET)
                .withHeader(ACCEPT, APPLICATION_JSON))
                .respond(response().withStatusCode(500));

    }
}
