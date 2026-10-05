package com.dawn.order.infra.persistence;

import com.dawn.order.entity.ShippingZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaShippingZoneRepository extends JpaRepository<ShippingZone, Long> {
    List<ShippingZone> findByActiveTrue();

    Optional<ShippingZone> findByZoneCode(String zoneCode);
}
