package uk.gov.pay.api.it;


import com.google.common.collect.ImmutableMap;
import com.jayway.jsonassert.JsonAssert;
import com.jayway.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

public class PaymentResourceSearchValidationITest extends PaymentResourceITestBase {

    private static final String VALID_REFERENCE = "test_reference";
    private static final String VALID_STATUS = "succeeded";
    private static final String VALID_FROM_DATE = "2016-01-28T00:00:00Z";
    private static final String VALID_TO_DATE = "2016-01-28T12:00:00Z";

    private static final String SEARCH_PATH = "/v1/payments";

    @Before
    public void mapBearerTokenToAccountId() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void searchPayments_errorWhenToDateIsNotInZoneDateTimeFormat() throws Exception {

        InputStream body = searchPayments(BEARER_TOKEN,
                ImmutableMap.of("reference", VALID_REFERENCE, "status", VALID_STATUS, "from_date", VALID_FROM_DATE, "to_date", "2016-01-01 00:00"))
                .statusCode(422)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0401"))
                .assertThat("$.description", is("Invalid parameters: to_date. See Public API documentation for the correct data formats"));
    }

    @Test
    public void searchPayments_errorWhenFromDateIsNotInZoneDateTimeFormat() throws Exception {

        InputStream body = searchPayments(BEARER_TOKEN,
                ImmutableMap.of("reference", VALID_REFERENCE, "status", VALID_STATUS, "from_date", "2016-01-01 00:00", "to_date", VALID_TO_DATE))
                .statusCode(422)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0401"))
                .assertThat("$.description", is("Invalid parameters: from_date. See Public API documentation for the correct data formats"));
    }

    @Test
    public void searchPayments_errorWhenStatusNotMatchingWithExpectedExternalStatuses() throws Exception {

        InputStream body = searchPayments(BEARER_TOKEN,
                ImmutableMap.of("reference", VALID_REFERENCE, "status", "invalid status", "from_date", VALID_FROM_DATE, "to_date", VALID_TO_DATE))
                .statusCode(422)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0401"))
                .assertThat("$.description", is("Invalid parameters: status. See Public API documentation for the correct data formats"));
    }

    @Test
    public void searchPayments_errorWhenReferenceSizeIsLongerThan255() throws Exception {

        InputStream body = searchPayments(BEARER_TOKEN,
                ImmutableMap.of("reference", RandomStringUtils.randomAlphanumeric(256), "status", VALID_STATUS, "from_date", VALID_FROM_DATE, "to_date", VALID_TO_DATE))
                .statusCode(422)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0401"))
                .assertThat("$.description", is("Invalid parameters: reference. See Public API documentation for the correct data formats"));
    }

    @Test
    public void searchPayments_errorWhenToDateNotInZoneDateTimeFormat_andInvalidStatus() throws Exception {

        InputStream body = searchPayments(BEARER_TOKEN,
                ImmutableMap.of("reference", VALID_REFERENCE, "status", "invalid status", "from_date", VALID_FROM_DATE, "to_date", "2016-01-01 00:00"))
                .statusCode(422)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0401"))
                .assertThat("$.description", is("Invalid parameters: status, to_date. See Public API documentation for the correct data formats"));
    }

    @Test
    public void searchPayments_errorWhenFromAndToDatesAreNotInZoneDateTimeFormat() throws Exception {

        InputStream body = searchPayments(BEARER_TOKEN,
                ImmutableMap.of("reference", VALID_REFERENCE, "status", VALID_STATUS, "from_date", "12345", "to_date", "2016-01-01 00:00"))
                .statusCode(422)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0401"))
                .assertThat("$.description", is("Invalid parameters: from_date, to_date. See Public API documentation for the correct data formats"));
    }

    @Test
    public void searchPayments_errorWhenAllFieldsAreInvalid() throws Exception {

        InputStream body = searchPayments(BEARER_TOKEN,
                ImmutableMap.of("reference", RandomStringUtils.randomAlphanumeric(256), "status", "invalid status", "from_date", "12345", "to_date", "98765"))
                .statusCode(422)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0401"))
                .assertThat("$.description", is("Invalid parameters: status, reference, from_date, to_date. See Public API documentation for the correct data formats"));
    }

    private ValidatableResponse searchPayments(String bearerToken, ImmutableMap<String, String> queryParams) {
        return given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .queryParameters(queryParams)
                .get(SEARCH_PATH)
                .then();
    }
}
