package com.dawn.inventory.port.out;

import com.dawn.inventory.entity.Stocktake;

import java.util.Optional;

public interface StocktakeRepository {
    Stocktake save(Stocktake stocktake);
    Optional<Stocktake> findById(Long id);
}
