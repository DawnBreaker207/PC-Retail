package com.dawn.order.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.order.entity.ShippingZone;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.infra.persistence.JpaShippingZoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    @Mock
    JpaShippingZoneRepository zoneRepo;

    private ShippingService service() {
        return new ShippingService(zoneRepo);
    }

    private ShippingZone zone(String code, String city, String fee) {
        return ShippingZone.builder().zoneCode(code).matchCity(city)
                .fee(new BigDecimal(fee)).active(true).build();
    }

    @Test
    @DisplayName("PICKUP: phí luôn 0, không cần zone")
    void pickup_zero() {
        assertThat(service().resolveFee(FulfillmentType.PICKUP, null)).isEqualByComparingTo(BigDecimal.ZERO);

        verifyNoInteractions(zoneRepo);
    }

    @Test
    @DisplayName("DELIVERY đúng city: lấy phí zone đó")
    void delivery_matchCity() {
        when(zoneRepo.findByActiveTrue()).thenReturn(List.of(
                zone("HCM", "HCM", "25000"), zone("DEFAULT", null, "30000")));

        assertThat(service().resolveFee(FulfillmentType.DELIVERY, Map.of("city", "hcm")))
                .isEqualByComparingTo(new BigDecimal("25000"));
    }

    @Test
    @DisplayName("DELIVERY city lạ, có zone catch-all: dùng catch-all")
    void delivery_fallbackCatchAll() {
        when(zoneRepo.findByActiveTrue()).thenReturn(List.of(zone("DEFAULT", null, "30000")));

        assertThat(service().resolveFee(FulfillmentType.DELIVERY, Map.of("city", "Hue")))
                .isEqualByComparingTo(new BigDecimal("30000"));
    }

    @Test
    @DisplayName("DELIVERY không zone nào khớp: chặn checkout 400")
    void delivery_unresolved_blocks() {
        when(zoneRepo.findByActiveTrue()).thenReturn(List.of(zone("HCM", "HCM", "25000")));

        assertThatThrownBy(() -> service().resolveFee(FulfillmentType.DELIVERY, Map.of("city", "Hanoi")))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("Chưa seed zone nào: fallback 30000 thay vì nổ")
    void noZones_fallback() {
        when(zoneRepo.findByActiveTrue()).thenReturn(List.of());

        assertThat(service().resolveFee(FulfillmentType.DELIVERY, Map.of("city", "HCM")))
                .isEqualByComparingTo(new BigDecimal("30000"));
    }
}
