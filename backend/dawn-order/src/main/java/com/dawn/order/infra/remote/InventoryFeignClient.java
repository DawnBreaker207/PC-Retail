package com.dawn.order.infra.remote;

import com.dawn.common.feign.FeignClientConfig;
import com.dawn.common.contract.StockDecrementCommand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "dawn-inventory",
        contextId = "orderInventoryFeignClient",
        url = "${app.services.inventory-url:http://dawn-inventory:8082}",
        path = "/api/v1/internal/inventory",
        configuration = FeignClientConfig.class,
        fallback = InventoryFeignClientFallback.class
)
public interface InventoryFeignClient {

    @PostMapping("/decrement-batch")
    List<Long> decrementBatch(@RequestBody List<StockDecrementCommand> items);

    @PostMapping("/increment-batch")
    void incrementBatch(@RequestBody List<StockDecrementCommand> items);

    @PostMapping("/reserve")
    void reserve(@RequestBody List<StockDecrementCommand> items, @RequestParam("orderId") Long orderId);

    @PostMapping("/release")
    void release(@RequestBody List<StockDecrementCommand> items, @RequestParam("orderId") Long orderId);

    @PostMapping("/deduct")
    void deduct(@RequestBody List<StockDecrementCommand> items, @RequestParam("orderId") Long orderId);

    @PostMapping("/restock")
    void restock(@RequestBody List<StockDecrementCommand> items, @RequestParam(value = "actorId", required = false) Long actorId);

    @GetMapping("/{skuId}/available")
    Integer available(@PathVariable("skuId") Long skuId);
}
