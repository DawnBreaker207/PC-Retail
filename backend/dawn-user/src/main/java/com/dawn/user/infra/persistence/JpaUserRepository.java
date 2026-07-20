package com.dawn.user.infra.persistence;

import com.dawn.user.entity.User;
import com.dawn.user.port.out.UserRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepositoryPort {

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    @Override
    default User save(User user) {
        return saveAndFlush(user);
    }
}
