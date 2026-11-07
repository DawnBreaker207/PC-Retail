package com.dawn.inventory.infra.persistence;

import com.dawn.inventory.entity.Stocktake;
import com.dawn.inventory.port.out.StocktakeRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStocktakeRepository extends JpaRepository<Stocktake, Long>, StocktakeRepository {
}
