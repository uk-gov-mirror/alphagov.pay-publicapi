package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.directdebit.mandates.AgreementError;
import uk.gov.pay.api.model.directdebit.mandates.CreateMandateRequest;
import uk.gov.pay.api.model.directdebit.mandates.CreateMandateResponse;
import uk.gov.pay.api.model.directdebit.mandates.GetMandateResponse;
import uk.gov.pay.api.resources.error.ApiErrorResponse;
import uk.gov.pay.api.service.MandatesService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Api(value = "/", description = "Public Api Endpoints for an agreements")
@Produces({"application/json"})
public class MandatesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MandatesResource.class);

    private final String baseUrl;
    private final MandatesService mandateService;


    @Inject
    public MandatesResource(PublicApiConfig configuration, MandatesService mandateService) {
        this.baseUrl = configuration.getBaseUrl();
        this.mandateService = mandateService;
    }

    @GET
    @Timed
    @Path("/v1/directdebit/mandates/{mandateId}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Find mandate by ID",
            notes = "Return information about the payment " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = GetMandateResponse.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = AgreementError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = AgreementError.class)})
    public Response getPayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
            @PathParam("mandateId") String mandateId) {
        LOGGER.info("Mandate get request - [ {} ]", mandateId);
        GetMandateResponse getMandateResponse = mandateService.get(account, mandateId);
        LOGGER.info("Mandate returned (created): [ {} ]", getMandateResponse);
        return Response.ok().entity(getMandateResponse).build();
    }
    
    @POST
    @Timed
    @Path("/v1/directdebit/mandates")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Create a new mandates",
            notes = "Create a new mandates",
            code = 201,
            nickname = "newAgreement")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = CreateMandateResponse.class),
            @ApiResponse(code = 400, message = "Bad request", response = PaymentError.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response createNewAgreement(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                       @ApiParam(value = "requestPayload", required = true) @Valid CreateMandateRequest createMandateRequest) {
        LOGGER.info("Mandate create request - [ {} ]", createMandateRequest);
        CreateMandateResponse createMandateResponse = mandateService.create(account, createMandateRequest);
        URI mandateUri = UriBuilder.fromUri(baseUrl)
                .path("/v1/directdebit/mandates/{mandateId}")
                .build(createMandateResponse.getMandateId());
        LOGGER.info("Mandate returned (created): [ {} ]", createMandateResponse);
        return Response.created(mandateUri).entity(createMandateResponse).build();
    }
}
