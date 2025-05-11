package com.tekkminds.awscourse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tekkminds.awscourse.services.CognitoUserService;
import com.tekkminds.awscourse.services.DecryptionUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
public class LoginHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson mapper = new Gson();
    private final CognitoUserService cognitoUserService = new CognitoUserService(System.getenv("AWS_REGION"));

    private final String appClientId = DecryptionUtils.decryptEnvironmentVariableByKey("MY_COGNITO_POOL_APP_CLIENT_ID");
    private final String appClientSecret = DecryptionUtils.decryptEnvironmentVariableByKey("MY_COGNITO_POOL_APP_CLIENT_SECRET");


    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        var logger = context.getLogger();
        var httpHeaders = Map.of(
                "Content-Type", "application/json"
        );
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(httpHeaders);

        try {
            var loginDetails = JsonParser.parseString(input.getBody()).getAsJsonObject();
            var loginResult = cognitoUserService.userLogin(loginDetails, appClientId, appClientSecret);

            response.withBody(mapper.toJson(loginResult, JsonObject.class));
            response.withStatusCode(200);

        } catch (AwsServiceException ex) {
            logger.log(ex.awsErrorDetails().errorMessage());
            ErrorResponse errorResponse = new ErrorResponse(ex.awsErrorDetails().errorMessage());
            String errorResponseJsonString = mapper.toJson(errorResponse, ErrorResponse.class);
            response.withBody(errorResponseJsonString);
            response.withStatusCode(ex.awsErrorDetails().sdkHttpResponse().statusCode());
        } catch (Exception ex) {
            logger.log(ex.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
            // In some cases ex.getMessage returns null. Gson would simply ignore these values during serialization.
            String errorResponseJsonString = new GsonBuilder().serializeNulls().create().toJson(errorResponse, ErrorResponse.class);
            response.withBody(errorResponseJsonString);
            response.withStatusCode(500);
        }

        return response;
    }
}
