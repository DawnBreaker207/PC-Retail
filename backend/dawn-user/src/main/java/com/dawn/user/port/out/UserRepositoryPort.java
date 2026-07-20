package com.dawn.user.port.out;

import com.dawn.user.entity.User;

import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    User save(User user);
}
