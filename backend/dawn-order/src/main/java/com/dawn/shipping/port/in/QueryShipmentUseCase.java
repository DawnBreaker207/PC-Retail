package com.dawn.shipping.port.in;

import com.dawn.common.response.ResponsePage;
import com.dawn.shipping.dto.ShippingFeeRequest;
import com.dawn.shipping.dto.ShippingFeeResponse;
import com.dawn.shipping.dto.ShippingOrderDto;
import org.springframework.data.domain.Pageable;

public interface QueryShipmentUseCase {

    ShippingFeeResponse calculateFee(ShippingFeeRequest req);

    ShippingOrderDto getByOrderId(Long orderId);

    ResponsePage<ShippingOrderDto> listByUser(Long userId, Pageable pageable);
}
