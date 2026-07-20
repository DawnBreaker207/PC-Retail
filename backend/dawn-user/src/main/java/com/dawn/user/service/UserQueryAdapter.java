package com.dawn.user.service;

import com.dawn.user.dto.UserMapper;
import com.dawn.user.dto.UserSummaryDto;
import com.dawn.user.infra.persistence.JpaUserRepository;
import com.dawn.user.port.out.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {

    private final JpaUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDto getUserSummary(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .map(UserMapper::toSummary)
                .orElse(null);
    }
}
