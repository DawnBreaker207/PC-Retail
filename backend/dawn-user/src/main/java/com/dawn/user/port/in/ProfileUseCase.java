package com.dawn.user.port.in;

import com.dawn.user.controller.request.UpdateProfileRequest;
import com.dawn.user.controller.response.UserResponse;

public interface ProfileUseCase {
    UserResponse getCurrentUser();
    UserResponse updateProfile(UpdateProfileRequest request);
    void deactivateAccount();
}
