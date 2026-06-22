package com.dawn.user.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.user.controller.request.AddressRequest;
import com.dawn.user.controller.request.UpdateProfileRequest;
import com.dawn.user.controller.response.UserResponse;
import com.dawn.user.dto.AddressDto;
import com.dawn.user.port.in.UserProfileUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileUseCase userProfileUseCase;

    @GetMapping("/me")
    public ResponseObject<UserResponse> getCurrentUser() {
        return ResponseObject.success(userProfileUseCase.getCurrentUser());
    }

    @PutMapping("/me")
    public ResponseObject<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseObject.success(userProfileUseCase.updateProfile(request));
    }

    @GetMapping("/me/addresses")
    public ResponseObject<List<AddressDto>> getAddresses() {
        return ResponseObject.success(userProfileUseCase.getAddresses());
    }

    @PostMapping("/me/addresses")
    public ResponseObject<AddressDto> addAddress(@Valid @RequestBody AddressRequest request) {
        return ResponseObject.created(userProfileUseCase.addAddress(request));
    }

    @PutMapping("/me/addresses/{addressId}")
    public ResponseObject<AddressDto> updateAddress(
            @PathVariable String addressId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseObject.success(userProfileUseCase.updateAddress(addressId, request));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseObject<Void> deleteAddress(@PathVariable String addressId) {
        userProfileUseCase.deleteAddress(addressId);
        return ResponseObject.deleted();
    }
}
