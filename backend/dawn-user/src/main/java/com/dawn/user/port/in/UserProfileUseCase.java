package com.dawn.user.port.in;

import com.dawn.user.controller.request.AddressRequest;
import com.dawn.user.controller.request.UpdateProfileRequest;
import com.dawn.user.controller.response.UserResponse;
import com.dawn.user.dto.AddressDto;

import java.util.List;

public interface UserProfileUseCase {

    UserResponse getCurrentUser();

    UserResponse updateProfile(UpdateProfileRequest request);

    List<AddressDto> getAddresses();

    AddressDto addAddress(AddressRequest request);

    AddressDto updateAddress(String addressId, AddressRequest request);

    void deleteAddress(String addressId);
}
