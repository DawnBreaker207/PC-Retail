package com.dawn.promotion.infra.redis;

import com.dawn.common.message.Message;
import com.dawn.promotion.port.out.PromotionCachePort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PromotionCacheAdapter implements PromotionCachePort {
    private static final String VOUCHER_KEY_PREFIX = "voucher:remaining:";
    private static final String FLASHSALE_KEY_PREFIX = "flashsale:remaining:";

    private final StringRedisTemplate redisTemplate;

    private DefaultRedisScript<List> decrementScript;
    private DefaultRedisScript<Long> rollbackScript;

    @PostConstruct
    public void init() {
        //
        decrementScript = new DefaultRedisScript<>();
        decrementScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/promotion_decrement.lua")));
        decrementScript.setResultType(List.class);

        //
        rollbackScript = new DefaultRedisScript<>();
        rollbackScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/promotion_rollback.lua")));
        rollbackScript.setResultType(Long.class);
    }


    @Override
    public void setVoucherRemaining(String code, int quantity) {
        redisTemplate.opsForValue().set(VOUCHER_KEY_PREFIX + code, String.valueOf(quantity), Duration.ofDays(7));
    }

    @Override
    public void deleteVoucher(String code) {
        redisTemplate.delete(VOUCHER_KEY_PREFIX + code);
    }

    @Override
    public Integer getVoucherRemaining(String code) {
        String cached = redisTemplate.opsForValue().get(VOUCHER_KEY_PREFIX + code);
        return cached != null ? Integer.parseInt(cached) : null;
    }

    @Override
    public void decrementPromotions(List<String> voucherCodes, List<Long> flashSaleSkuIds) {
        List<String> keys = new ArrayList<>();
        List<String> args = new ArrayList<>();

        for (String code : voucherCodes) {
            keys.add(VOUCHER_KEY_PREFIX + code);
            args.add("1");
        }
        for (Long skuId : flashSaleSkuIds) {
            keys.add(FLASHSALE_KEY_PREFIX + skuId);
            args.add("1");
        }

        if (keys.isEmpty()) return;

        List<String> failedKeys = redisTemplate.execute(decrementScript, keys, args.toArray());

        if (failedKeys != null && !failedKeys.isEmpty()) {
            throw new IllegalStateException(Message.format(Message.Promotion.PROMOTION_OUT_OF_STOCK, failedKeys));
        }
    }

    @Override
    public void incrementPromotions(List<String> voucherCodes, List<Long> flashSaleSkuIds) {
        List<String> keys = new ArrayList<>();
        List<String> args = new ArrayList<>();

        for (String code : voucherCodes) {
            keys.add(VOUCHER_KEY_PREFIX + code);
            args.add("1");
        }
        for (Long skuId : flashSaleSkuIds) {
            keys.add(FLASHSALE_KEY_PREFIX + skuId);
            args.add("1");
        }

        if (keys.isEmpty()) return;

        redisTemplate.execute(rollbackScript, keys, args.toArray());
    }
}
