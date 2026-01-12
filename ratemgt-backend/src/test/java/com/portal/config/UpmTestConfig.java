package com.portal.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@TestConfiguration
public class UpmTestConfig {

    @Bean
    @Primary
    public RestTemplate upmRestTemplate() {
        RestTemplate rt = new RestTemplate();

        String auth = envOrDefault(
                "UPM_AUTH_BASIC",
                "Basic cmF0ZXN5c3RlbTpJaTViNjhMbXBkS1M2clVZOFVmRWE1U2N3TWhWRkcxV0JwYjFRazV5WDA2RVVXVE91akFRZDNUUW84dmVuTjVRWDBGSm1XSTZlUm9rTnVSSlZ1bEQxYjFJdWFpYU1BT0hOSzNXR2p4emJPKzVlU25XZm5YM3JmWWY3ejdHQVpNTg=="
        );
        String serviceName = envOrDefault("UPM_SERVICE_NAME", "ADAuthentication");
        String tokenId = envOrDefault("UPM_TOKEN_ID", "0");

        ClientHttpRequestInterceptor headerInterceptor = new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(org.springframework.http.HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                request.getHeaders().set("Authorization", auth);
                request.getHeaders().set("ServiceName", serviceName);
                request.getHeaders().set("TokenID", tokenId);
                return execution.execute(request, body);
            }
        };

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(headerInterceptor);
        rt.setInterceptors(interceptors);
        return rt;
    }

    private static String envOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}

