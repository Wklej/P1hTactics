package com.p1h.p1htactics.config;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@Component
public class ApiKeyProvider {
    private final SsmClient ssmClient = SsmClient.create();

    public String getApiKey() {
        GetParameterRequest request = GetParameterRequest.builder()
                .name("/p1htactics-key")
                .withDecryption(true)
                .build();
        GetParameterResponse response = ssmClient.getParameter(request);
        return response.parameter().value();
    }
}
