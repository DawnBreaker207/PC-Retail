package com.dawn.inventory.infra.redis;

import com.dawn.inventory.dto.StockDecrementCommand;
import com.dawn.inventory.port.out.InventoryCachePort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCacheAdapter implements InventoryCachePort {

    private static final String KEY_PREFIX = "inventory:available";

    private final StringRedisTemplate redisTemplate;

    private DefaultRedisScript<List> decrementScript;
    private DefaultRedisScript<Long> rollbackScript;

    @PostConstruct
    void loadScripts() {
        // Load Decrement Script
        decrementScript = new DefaultRedisScript<>();
        decrementScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/inventory_decrement.lua")));
        decrementScript.setResultType(List.class);

        // Load Rollback Script
        rollbackScript = new DefaultRedisScript<>();
        rollbackScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/inventory_rollback.lua")));
        rollbackScript.setResultType(Long.class);
    }

    @Override
    public void setStock(Long skuId, int quantity) {
        redisTemplate.opsForValue().set(KEY_PREFIX + skuId, String.valueOf(quantity));
    }

    @Override
    public List<Long> decrementBatch(List<StockDecrementCommand> items) {
        List<String> keys = items.stream()
                .map(i -> String.valueOf(i.skuId()))
                .toList();

        String[] args = items.stream()
                .map(i -> String.valueOf(i.quantity()))
                .toArray(String[]::new);

        List result = redisTemplate.execute(decrementScript, keys, (Object[]) args);
        if (result == null) return List.of();

        return result.stream()
                .map(id -> Long.parseLong(id.toString()))
                .toList();

    }

    @Override
    public void incrementBatch(List<StockDecrementCommand> items) {
        List<String> keys = items.stream()
                .map(i -> String.valueOf(i.skuId()))
                .toList();
        String[] args = items.stream()
                .map(i -> String.valueOf(i.quantity()))
                .toArray(String[]::new);

        redisTemplate.execute(rollbackScript, keys, (Object[]) args);
    }

    @Override
    public void syncFromDb(Long skuId, int availableQuantity) {
        redisTemplate.opsForValue().set(KEY_PREFIX + skuId, String.valueOf(availableQuantity));
    }

    @Override
    public Integer getAvailable(Long skuId) {
        String val = redisTemplate.opsForValue().get(KEY_PREFIX + skuId);
        return val != null ? Integer.parseInt(val) : null;
    }
}
