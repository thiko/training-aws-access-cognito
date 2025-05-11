package com.tekkminds.awscourse.services;

import com.google.gson.JsonObject;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;


public class CognitoUserService {

    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;

    public CognitoUserService(String region) {
        this.cognitoIdentityProviderClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .build();
    }

    public JsonObject userLogin(JsonObject loginDetails, String appClientId, String appClientSecret) {
        String email = loginDetails.get("username").getAsString();
        String password = loginDetails.get("password").getAsString();
        String generatedSecretHash = calculateSecretHash(appClientId, appClientSecret, email);

        var authParams = Map.of(
                "USERNAME", email,
                "PASSWORD", password,
                "SECRET_HASH", generatedSecretHash
        );

        // TODO: Initialize the InitiateAuthRequest using its builder() method
        //  set client id, auth flow (AuthFlowType.USER_PASSWORD_AUTH) and auth params

        // TODO: Send the request using cognitoIdentityProviderClient.initiateAuth(initiateAuthRequest);

        // TODO: Send the request using initiateAuthResponse.authenticationResult()

        JsonObject loginUserResult = new JsonObject();
        // TODO: add some parameters

        // loginUserResult.addProperty("isSuccessful", initiateAuthResponse.sdkHttpResponse().isSuccessful());
        // add idToken, accessToken and refreshToken

        return loginUserResult;
    }

    public String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, String userName) {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

        var signingKey = new SecretKeySpec(
                userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM);
        try {
            var mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating secret hash", e);
        }
    }

}
