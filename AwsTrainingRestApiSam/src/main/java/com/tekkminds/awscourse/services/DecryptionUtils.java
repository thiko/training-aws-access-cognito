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
