package com.dawn.order.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.order.entity.ShippingZone;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.infra.persistence.JpaShippingZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// docs §13.3: BE tính lại + ghi đè fee — không tin số FE gửi.
// PICKUP = 0 tường minh. Không resolve được zone → chặn checkout.
@Service
@RequiredArgsConstructor
public class ShippingService {

    private final JpaShippingZoneRepository zoneRepo;

    public BigDecimal resolveFee(FulfillmentType type, Map<String, Object> address) {
        if (type == FulfillmentType.PICKUP) return BigDecimal.ZERO;
        String city = address == null ? null : (String) address.get("city");
        List<ShippingZone> zones = zoneRepo.findByActiveTrue();
        if (zones.isEmpty()) return BigDecimal.valueOf(30000); // fallback khi chưa seed zone
        for (ShippingZone z : zones) {
            if (z.getMatchCity() == null || (city != null && city.equalsIgnoreCase(z.getMatchCity()))) {
                return z.getFee();
            }
        }
        throw new InvalidRequestException("Khu vực giao hàng chưa hỗ trợ: " + city);
    }
}
