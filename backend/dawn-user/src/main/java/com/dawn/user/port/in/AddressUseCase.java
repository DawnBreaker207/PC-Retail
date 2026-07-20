package com.dawn.user.port.in;

import com.dawn.user.controller.request.AddressRequest;
import com.dawn.user.dto.AddressDto;

import java.util.List;

public interface AddressUseCase {
    List<AddressDto> getAddresses();
    AddressDto addAddress(AddressRequest request);
    AddressDto updateAddress(String addressId, AddressRequest request);
    void deleteAddress(String addressId);
}
