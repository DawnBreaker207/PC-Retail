package com.dawn.order.infra.remote;

import com.dawn.common.web.InternalRestClient;
import com.dawn.order.dto.StockDecrementCommand;
import com.dawn.order.port.out.InventoryClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class InventoryRemoteAdapter implements InventoryClientPort {

    private final InternalRestClient client;
    private final String baseUrl;

    public InventoryRemoteAdapter(InternalRestClient client,
                                  @Value("${app.services.inventory-url}") String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    @Override
    public List<Long> decrementBatch(List<StockDecrementCommand> items) {
        return client.exchange(baseUrl + "/api/v1/internal/inventory/decrement-batch", HttpMethod.POST, items,
                new ParameterizedTypeReference<>() {
                });
    }

    @Override
    public void incrementBatch(List<StockDecrementCommand> items) {
        post("/increment-batch", items);
    }

    @Override
    public void reserve(List<StockDecrementCommand> items, Long orderId) {
        post("/reserve?orderId=" + orderId, items);
    }

    @Override
    public void release(List<StockDecrementCommand> items, Long orderId) {
        post("/release?orderId=" + orderId, items);
    }

    @Override
    public void deduct(List<StockDecrementCommand> items, Long orderId) {
        post("/deduct?orderId=" + orderId, items);
    }

    @Override
    public void restock(List<StockDecrementCommand> items, Long actorId) {
        post("/restock" + (actorId == null ? "" : "?actorId=" + actorId), items);
    }

    private void post(String path, Object body) {
        client.exchange(baseUrl + "/api/v1/internal/inventory" + path, HttpMethod.POST, body,
                new ParameterizedTypeReference<Void>() {
                });
    }
}
