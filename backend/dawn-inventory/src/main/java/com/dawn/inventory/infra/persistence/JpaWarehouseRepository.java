package com.dawn.inventory.infra.persistence;

import com.dawn.inventory.entity.Warehouse;
import com.dawn.inventory.port.out.WarehouseRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaWarehouseRepository extends JpaRepository<Warehouse, Long>, WarehouseRepository {

    @Override
    @Query("SELECT w FROM Warehouse w WHERE w.active = true")
    List<Warehouse> findAllActive();
}
