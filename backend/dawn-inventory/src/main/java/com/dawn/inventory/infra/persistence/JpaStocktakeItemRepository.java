package com.dawn.inventory.infra.persistence;

import com.dawn.inventory.entity.StocktakeItem;
import com.dawn.inventory.port.out.StocktakeItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaStocktakeItemRepository extends JpaRepository<StocktakeItem, Long>, StocktakeItemRepository {
    List<StocktakeItem> findByStocktakeId(Long stocktakeId);

    @Override
    default void saveAll(List<StocktakeItem> items) {
        saveAllAndFlush(items);
    }
}
