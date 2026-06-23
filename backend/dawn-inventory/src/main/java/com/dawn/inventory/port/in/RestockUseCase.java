package com.dawn.inventory.port.in;

import java.math.BigDecimal;

public interface RestockUseCase {
    void restock(Long skuId, int quantity, BigDecimal unitCost, Long userId);
}
