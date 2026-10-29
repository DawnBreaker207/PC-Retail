package com.dawn.ai.infra.persistence;

import com.dawn.ai.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserPreferenceRepository extends JpaRepository<UserPreference, Long> {
}
