# Lab 4 API Gateway + Lambda + Cognito


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
- Select your App Client and edit it. For Authentication-Flows select `ALLOW_ADMIN_USER_PASSWORD_AUTH` as we will use it in our Lambda. 
- Save your changes

## 4 Create a new Cognito User
Back to your User pool, click on Users (Benutzer) and then **Create a new User**.
- Enter a valid e-mail address
- Mark the E-Mail as verified
- Give it a password. I'll use Test12345.
- Save the changes by **Create User**

## 5 Implement the Login Handler
In this lecture, you will need some more dependencies from the AWS SDK as well as google GSON. 
- `cognitoidentity` and `cognitoidentityprovider` to work with Cogntio (we will use the Cognito API)
- `kms` is not needed in the first place but can be useful for later usage.
- `gson` to serialize Java classes to JSON (and vice versa)
- `apache-client` for http requests
