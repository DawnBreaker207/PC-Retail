package com.dawn.inventory.port.out;

import com.dawn.inventory.entity.Warehouse;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository {
    Optional<Warehouse> findById(Long id);
    List<Warehouse> findAllActive();
    Warehouse save(Warehouse warehouse);
}
