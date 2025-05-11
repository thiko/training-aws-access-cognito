package com.tekkminds.awscourse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;

import java.util.Map;

/**
 * Handler for Login requests.
 */
public class LoginHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson mapper = new Gson();
    // TODO: instantiate your CognitoUserService - Hint: There is an environment variable called AWS_REGION
    //  Reading environment variables in Java: System.getenv(...)

    // TODO: Read the client id and client secret from your environment variables

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        var logger = context.getLogger();
        var httpHeaders = Map.of(
                "Content-Type", "application/json"
        );

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(httpHeaders);
        try {

            // TODO: Read the request payload as JsonObject: com.google.gson.JsonParser.parseString(input.getBody()).getAsJsonObject();
            // TODO: Login using your CognitoUserService

            // TODO: Build a response using the services call details
            //  the mapper offers a toJson(...) method.

            return response
                    .withStatusCode(200);
        } catch (Exception e) {

            // TODO: Use the ErrorResponse record to build a proper error response

            return response
                    .withBody("{}")
                    .withStatusCode(500);
        }
    }
}
