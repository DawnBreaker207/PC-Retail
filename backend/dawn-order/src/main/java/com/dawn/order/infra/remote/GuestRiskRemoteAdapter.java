package com.dawn.order.infra.remote;

import com.dawn.common.web.InternalRestClient;
import com.dawn.order.port.out.GuestRiskPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class GuestRiskRemoteAdapter implements GuestRiskPort {

    private final InternalRestClient client;
    private final String baseUrl;

    public GuestRiskRemoteAdapter(InternalRestClient client,
                                  @Value("${app.services.user-url}") String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    @Override
    public boolean isPhoneVerified(String phone) {
        return Boolean.TRUE.equals(client.exchange(
                baseUrl + "/api/v1/internal/risk/guest-phone/verified?phone=" + phone,
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }));
    }

    @Override
    public void consumePhoneVerification(String phone) {
        client.exchange(baseUrl + "/api/v1/internal/risk/guest-phone/consume?phone=" + phone,
                HttpMethod.POST, null, new ParameterizedTypeReference<Void>() {
                });
    }

    @Override
    public long phoneAbuseCount(String phone) {
        Long count = client.exchange(baseUrl + "/api/v1/internal/risk/guest-phone/abuse?phone=" + phone,
                HttpMethod.GET, null, new ParameterizedTypeReference<Long>() {
                });
        return count == null ? 0 : count;
    }

    @Override
    public void recordPhoneAbuse(String phone) {
        client.exchange(baseUrl + "/api/v1/internal/risk/guest-phone/abuse?phone=" + phone,
                HttpMethod.POST, null, new ParameterizedTypeReference<Void>() {
                });
    }
}
