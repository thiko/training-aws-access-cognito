# Exercise: Securing APIs with AWS Cognito, API Gateway and Lambda

## Objective
Learn how to implement user authentication for your serverless APIs using Amazon Cognito, and how to securely handle sensitive configuration using AWS Key Management Service (KMS).

## Overview
In this exercise, you'll create an Amazon Cognito User Pool to manage user authentication, then integrate it with a Lambda function that verifies user credentials. You'll also learn how to securely handle sensitive information like client IDs and secrets using AWS KMS for encryption.

## Prerequisites
- Completed the previous SAM Lambda exercise or access to the provided sample project
- Basic understanding of authentication concepts

## Steps

### Part 1: Deploy the Base Application

1. **Get the project code**
   - If you completed the previous lab, you can re-use the project but please make sure to copy the `LoginHandler`, `CognitoUserService` and dependencies from `pom.xml` out of this repository.
   - If you want to start from scratch, clone this sample project:
     ```bash
     # Clone the repository with the sample project
     git clone https://github.com/thiko/training-aws-lambda-sam-01/tree/completed
     cd <project-directory>
     ```

2. **Deploy the application**
   - If you're using your previous project:
     ```bash
     sam build
     sam deploy
     ```
   - If you're using the sample project:
     ```bash
     sam build
     sam deploy --guided
     ```
      - Follow the prompts to configure your deployment
      - Make note of the stack name for later use

### Part 2: Create a Cognito User Pool

1. **Create a new User Pool**
   - Open the AWS Management Console
   - Navigate to the Amazon Cognito service
   - Select "User Pools" and click "Create user pool"
   - Stay on "Common Webapplication" application type
   - Enter a name for your user pool app client: `tekkminds-awscourse-app01`
   - Under "Cognito user pool sign-in options", select "Email" only
   - Set "email" and "phone_number" as required attributes
   - Click "Create Userpool"
   - Skip the Code examples (you can open the Login page in a new tab - we can use it later)

2. **Configure security requirements**
   - On the "Authentication methods" page:
   - Under "Password policy", select "Custom"
   - **For training purposes only**: Uncheck all password requirements
     > **Note**: In production environments, always maintain strong password requirements!
   - Keep other settings at their defaults
   - Click "Save"

3. **Nothing to configure in sign-up experience**

5. **Integrate your app**
   - Click on "App-Clients"
   - Select your app client (`tekkminds-awscourse-app01`)
   - Click on edit
   - Under "Authentication flows":
      - Select "ALLOW_USER_PASSWORD_AUTH"
      - This allows simple password-based authentication without requiring the user pool ID
   - Click "Save"

6. **Note down important information**
   - After creation, record the following:
      - User Pool ID
      - App client ID
      - App client secret (click "Show client secret" to reveal it)
   - You'll need these values for your Lambda function

### Part 3: Create a Cognito User

1. **Add a new user**
   - In the Cognito console, select your user pool
   - Go to the "Users" tab
   - Click "Create user"
   - Enter a valid email address (you'll use this as the username)
   - Deselect "Send an invitation to this new user?"
   - Select "Mark email as verified"
   - Set an initial password (e.g., `Test12345`)
   - Click "Create user"

2. **Complete first sign-in**
   - In the Cognito console, go to "App clients" and select your app
   - Find and click on "View Login" (top right)
   - Sign in with the email and temporary password
   - You'll be prompted to change your password
   - Set a new permanent password (e.g., `test1234`)

### Part 4: Understanding Authentication Flows

In this exercise, we use the USER_PASSWORD_AUTH flow, which has several advantages:

1. **Simplicity**: This flow is simpler to implement as it doesn't require the User Pool ID.

2. **Client-side friendly**: It can be used directly from mobile or web applications.

3. **Standard authentication model**: Username and password are sent directly to Cognito for verification.

Cognito supports several other authentication flows:

- **USER_SRP_AUTH**: Uses Secure Remote Password protocol to avoid sending the actual password over the network. Better for security-sensitive applications.

  > **Important Note for Production**: When developing real-world applications, especially frontend applications (browsers, mobile apps) or Single-Page Applications (SPAs) that authenticate directly with Cognito, you should strongly consider using USER_SRP_AUTH instead of USER_PASSWORD_AUTH. The SRP protocol provides significantly better security by never transmitting the actual password over the network, which is critical when authenticating from potentially unsecured client environments.

- **ADMIN_USER_PASSWORD_AUTH**: Requires the User Pool ID and must be called from a secure backend, not directly from clients. This flow uses the AdminInitiateAuth API.

- **REFRESH_TOKEN_AUTH**: Used to obtain new access tokens using a refresh token without requiring the user to sign in again.

Each flow has different security implications and use cases. While the USER_PASSWORD_AUTH flow used in this exercise is suitable for learning purposes, remember that USER_SRP_AUTH would be the recommended choice for production client-side applications.

### Part 5: Update the Lambda Function

1. **Update project dependencies**
   - When you re-use the project from a previous lab, please make sure to copy all necessary dependencies from this repository.
   - You have to do nothing if you did a checkout of this repository

2. **Add Cognito credentials to your Lambda function**
   - Open your `template.yaml` file
   - Add environment variables to your Lambda function resource:
     ```yaml
     Environment:
       Variables:
         MY_COGNITO_POOL_APP_CLIENT_ID: <Your-Client-ID>
         MY_COGNITO_POOL_APP_CLIENT_SECRET: <Your-Client-Secret>
     ```
   - Replace the placeholders with the values from your Cognito app client

3. **Update the `CognitoUserService` class**
   The `CognitoUserService` class is only partially implemented. Some important parts are missing. Fix it!

4. **Update the Lambda handler**
   The `LoginHandler` is also not done yet. Fill the gaps!

5. **Build and deploy the updated application**
   ```bash
   sam build
   sam deploy
   ```

### Part 6: Test the Integration

1. **Test the login endpoint**
   - Get the API Gateway endpoint URL from the SAM deployment output
   - Use Postman, curl, or another API tool to send a POST request
   - Set content type to `application/json`
   - Include the user credentials in the request body:
     ```json
     {
       "username": "your-email@example.com",
       "password": "your-password"
     }
     ```
   - Example curl command:
     ```bash
     curl -X POST https://your-api-id.execute-api.your-region.amazonaws.com/Prod/users/login \
     -H "Content-Type: application/json" \
     -d '{"username":"your-email@example.com","password":"your-password"}'
     ```

2. **Verify the response**
   - A successful authentication should return:
     ```json
     {
       "isSuccessful": true,
       "statusCode": 200,
       "idToken": "eyJraWQiOiI...",
       "accessToken": "eyJraWQiOiI...",
       "refreshToken": "eyJjdHkiOiJ..."
     }
     ```
   - An unsuccessful authentication will return an error message

### Part 7: Enhance Security with KMS Encryption (Optional)

1. **Create a KMS key**
   - Open the AWS KMS console
   - Navigate to "Customer managed keys"
   - Click "Create key"
   - Key type: Symmetric
   - Key usage: Encrypt and decrypt
   - Enter a name: `aws-training-rest-api`
   - For Key administrators, select your IAM user
   - For Key usage, select your Lambda execution role
   - Click "Finish" to create the key
   - Note the Key ID

2. **Encrypt your sensitive data**
   - Create a file named `secret` with your Cognito app client ID:
     ```bash
     echo -n "your-app-client-id" > secret
     ```
   - Encrypt it using the KMS key:
     ```bash
     aws kms encrypt --key-id <your-kms-key-id> --plaintext fileb://secret
     ```
   - Copy the CiphertextBlob value from the output
   - Repeat for your app client secret

3. **Update the template.yaml**
   - Replace the plaintext environment variables with the encrypted values:
     ```yaml
     Environment:
       Variables:
         MY_COGNITO_POOL_APP_CLIENT_ID: <encrypted-client-id>
         MY_COGNITO_POOL_APP_CLIENT_SECRET: <encrypted-client-secret>
     ```

4. **Add decryption utility**
   - Create a new class `DecryptionUtils.java` in the services package:

```java
package com.tekkminds.awscourse.services;

import org.apache.commons.codec.binary.Base64;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DecryptionUtils {

    /**
     * Simplified synchronous KMS decryption operation for environment variables.
     *
     * @param environmentVariableName - name of the environment variable. Value should be encrypted
     * @return Decrypted value as plaintext
     */
    public static String decryptEnvironmentVariableByKey(String environmentVariableName) {

        var encryptedKey = Base64.decodeBase64(System.getenv(environmentVariableName));

        try (var client = KmsClient.create()) {
            var request = DecryptRequest.builder()
                    .ciphertextBlob(SdkBytes.fromByteBuffer(ByteBuffer.wrap(encryptedKey)))
                    .build();
            // If you did not encrypt the environment variable manually but through the Lambda-Environment-Variable-UI,
            //  you could add the encryption context:
            //  .encryptionContext(Map.of("LambdaFunctionName", System.getenv("AWS_LAMBDA_FUNCTION_NAME")
            //  This adds another layer of security as this key could not be used by other Lambda functions.
            //  As we did not specify the encryption context during encryption in this lecture, we should not specify it here.


            var plainTextKey = client.decrypt(request).plaintext();
            return plainTextKey.asString(StandardCharsets.UTF_8).trim();
        }
    }
}
```

5. **Update the Lambda handler** (for encrypted environment variables)
   - Modify how you access the environment variables:
     ```java
     private final String appClientId = DecryptionUtils.decryptEnvironmentVariableByKey("MY_COGNITO_POOL_APP_CLIENT_ID");
     private final String appClientSecret = DecryptionUtils.decryptEnvironmentVariableByKey("MY_COGNITO_POOL_APP_CLIENT_SECRET");
     ```

6. **Update IAM permissions**
   You permitted the Lambda execution role during creation of the KMS Key already.
   Therefore it's not necessary to add any policy statement to the Lambda execution role using SAM.

7. **Build and deploy the updated application**
   ```bash
   sam build
   sam deploy
   ```

8. **Test the enhanced secure application**
   - Test the API again to verify that the decryption works correctly

### Part 8: Clean Up

1. **Delete the SAM application**
   ```bash
   sam delete <your-stack-name>
   ```

2. **Keep the Cognito User Pool for upcoming labs**

3. **Delete the KMS Key** (if created)
   - Navigate to the KMS console
   - Select your customer managed key
   - Click "Schedule key deletion"
   - Set a waiting period (minimum 7 days)
   - Confirm deletion

## Verification

You have successfully completed this exercise when:
- You've created a Cognito User Pool with an app client
- You've created a user in the pool
- Your Lambda function can authenticate users against Cognito
- You can successfully obtain authentication tokens by sending credentials to your API
- (Optional) You've implemented secure handling of sensitive data using KMS

## Common Issues and Troubleshooting

1. **Authentication failures**:
   - Double-check the user pool ID, client ID, and client secret
   - Ensure the user exists and has verified their email
   - Verify that the USER_PASSWORD_AUTH flow is enabled for your app client

2. **KMS decryption errors**:
   - Ensure the Lambda execution role has the necessary KMS permissions
   - Verify that the encrypted text is correctly formatted (Base64 encoded)
   - Check that you're using the correct KMS key

3. **Missing environment variables**:
   - Verify that your Lambda function configuration includes all required environment variables
   - Check for typos in environment variable names

## Extended Learning

1. **Implement token verification**:
   - Extend your API to verify JWT tokens issued by Cognito
   - Add a protected endpoint that requires a valid authentication token

2. **Add a Cognito Authorizer to API Gateway**:
   - Configure API Gateway to validate Cognito tokens automatically
   - This eliminates the need for manual token validation in your Lambda functions

3. **Implement refresh token functionality**:
   - Add an endpoint that uses refresh tokens to obtain new access tokens
   - This allows for longer user sessions without requiring re-authentication
