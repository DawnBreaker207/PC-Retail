package com.dawn.shipping.dto;

public record OrderItemLine(Long skuId, Integer quantity, Integer weightGrams) {}
