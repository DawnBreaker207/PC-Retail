package com.dawn.inventory.port.out;

import com.dawn.inventory.entity.StocktakeItem;

import java.util.List;

public interface StocktakeItemRepository {
    StocktakeItem save(StocktakeItem item);
    void saveAll(List<StocktakeItem> items);
    List<StocktakeItem> findByStocktakeId(Long stocktakeId);
}
