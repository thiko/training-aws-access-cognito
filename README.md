# Lab API Gateway + Lambda + Cognito


# Precondition
- Cognito UserPool has been created

## 1 Re-Deploy your application
Open the Lab02 project and deploy it again on your account:
- `sam build`
- `sam deploy`

In case you did not complete Lab 2, please checkout the project from: 
and then run:
- `sam build`
- `sam deploy --guided`

## 2 Create a Cognito User Pool
- Open the Cognito service in the browser
- Select Userpool (Benutzerpool) and then create
- Give your application a name (tekkminds-awscourse-app01)
- Select "E-Mail" as authentication option

After the Userpool has been created, select *Authentication* (Authentifizierung) and *Sign-In methods* (Authentifizierungsmethoden).
From there edit the password rules, click on *Custom* and unselect the password requirements.

**We will do that only for demo purposes - stay on restrict password requirements in real life scenarios!**

## 3 Configure Authentication Flow

### Flow descriptions
- `ALLOW_ADMIN_USER_PASSWORD_AUTH`: Authenticate user programmatically by sending Username, Password, ClientId and **Cognito UserPoolId** 
- `ALLOW_USER_SRP_AUTH`: With this option enabled, we would not send the users password to cognito directly. The client would have to generate a hash based on the password which is send (along the Username) to Cognito. This prevents vaious attacks like man-in-the-middle attacks and is in general recommended when working in insecure networks. On the other hand, the code is slightly more complex.
- `ALLOW_USER_PASSWORD_AUTH`: Username and password will be send during authentication. Similar to `ALLOW_ADMIN_USER_PASSWORD_AUTH` but without UserPoolId

### What to do
- Select your App Client and edit it. For Authentication-Flows select `USER_PASSWORD_AUTH` as we will use it in our Lambda. 
- Save your changes

## 4 Create a new Cognito User
Back to your User pool, click on Users (Benutzer) and then **Create a new User**.
- Enter a valid e-mail address
- Mark the E-Mail as verified
- Give it a password. I'll use Test12345.
- Save the changes by **Create User**
- Then open your App Client **App Clients -> Show Authenticationsite** in the top right corner.
- Login with your newly created user. You will be forced to change its password. Do it. I'll use `test1234`


## 5 Login Handler
In this lecture, you will need some more dependencies from the AWS SDK as well as google GSON. 
- `cognitoidentity` and `cognitoidentityprovider` to work with Cogntio (we will use the Cognito API)
- `kms` is not needed in the first place but can be useful for later usage.
- `gson` to serialize Java classes to JSON (and vice versa)
- `apache-client` for http requests
You can copy my `pom.xml` from: <github-path>

### 5.1 Required environment variables
In order to work with the Cognito API, we need the **Client-ID** and the **Client-Secret** in our Lambda function.
For the first iteration, we will make them available as plaintext enviornment variables. 
- Open your `template.yaml` file and add two environment variables to your Lambda:
```
    Environment: # More info about Env Vars: https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#environment-object
      Variables:
        MY_COGNITO_POOL_APP_CLIENT_ID: <Your-Client-ID>
        MY_COGNITO_POOL_APP_CLIENT_SECRET: <Your-Client-Secret>
```
Replace the placeholder with values taken from your Cognito Userpool **App Client**.

### 5.2 Implement a Service class
To encapsulate our code a little bit open `CognitoUserService`. Some parts has been implemented already. Try to fill the gaps.


# 6 Test it!
Again copy the URL of the Login Endpoint in your Prod stage from API-Gateway.
Send a POST request with `application/json` content type and your login credentials as payload.
```
{
  "username": "<your-email>",
  "password": "<your-passowrd>"
}
```
You should receive a JSON object with following structure:
```
{
  "isSuccessful": true,
  "statusCode": 200,
  "idToken": "",
  "accessToken": "",
  "refreshToken": ""
}
```

## 6 Optional: Encrypted environment variables
It's never a good idea to store sensitive data in environment variables in plaintext. We did exactly that.
There are better ways to do that! For example ...
- Store it in AWS Systems Manager **Param Store** as secret and let Lambda read it from there
- Store it in AWS Secret Manager (only if you plan to rotate it) and let Lambda read it from there
- Use the *AWS Parameters and Secrets Lambda Extension* (it caches parameters and secrets)
- Use encrypted values in your environment variables

In this lecture we will practice the last option (using encrypted environment variables).

### 6.1 Encrypt your secrets
Lets utilize the **Key Management Service (KMS)** to encrypt your values.

- First open the `Key Management Service` in the AWS Console UI. 
- Then click on `Customer Managed Keys` (Kundenverwaltete Schlüssel) and select `Create key` (Schlüssel erstellen)
- Give it a name like `aws-training-rest-api`
- For the permissions, give our Login Lambda function to **use** but not **manage** the keys
- Create the key
- Copy the **ID** of the newly created KMS Key. You need it soon

**Now lets encrypt your data:**
- Copy your value of **MY_COGNITO_POOL_APP_CLIENT_ID** 
- Create a new file *secret* and paste your app client id into. Only the value - no whitespaces, linebreaks etc.
- Open your CLI and encrpyt your client id using KMS: `aws kms encrypt --key-id <id-of-your-kms-key> --plaintext fileb://secret`
- This will (hopefuly) return a JSON object. Copy the value of *CyphertextBlob* as value of **MY_COGNITO_POOL_APP_CLIENT_ID** in your `template.yaml` file
- Do exactly the same for the **MY_COGNITO_POOL_APP_CLIENT_SECRET** (clear the *secret* file first)
- Delete the secret file

**Implement decrypt on Lambda**
You Lambda function does now receive the encrypted environment variables. We need some custom code to decrypt it.
Create a new class **DecryptionUtils** and use the following (simplified) implementation:

```java
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

            var plainTextKey = client.decrypt(request).plaintext();
            return plainTextKey.asString(StandardCharsets.UTF_8).trim();
        }
    }
}
```
(See AWS documentation for an advanced version: https://docs.aws.amazon.com/kms/latest/developerguide/example_kms_Decrypt_section.html)

Then update your Lambda code to utilize the decryption operation:
```java
private final String appClientId = DecryptionUtils.decryptEnvironmentVariableByKey("MY_COGNITO_POOL_APP_CLIENT_ID");
private final String appClientSecret = DecryptionUtils.decryptEnvironmentVariableByKey("MY_COGNITO_POOL_APP_CLIENT_SECRET");
```

Finally build and deploy your code! Is it still working? 