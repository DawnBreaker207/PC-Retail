package com.dawn.user.port.out;

import com.dawn.user.dto.UserSummaryDto;

public interface UserQueryPort {
    UserSummaryDto getUserSummary(Long userId);
}
