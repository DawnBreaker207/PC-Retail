package com.dawn.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CartUpdateEvent(Long userId, int version) {
    public CartUpdateEvent(Long userId) {
        this(userId, 1);
    }
}
