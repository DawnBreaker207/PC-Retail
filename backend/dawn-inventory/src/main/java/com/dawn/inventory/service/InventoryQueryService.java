package com.dawn.inventory.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.response.ResponsePage;
import com.dawn.inventory.dto.InventoryDto;
import com.dawn.inventory.dto.InventoryTransactionDto;
import com.dawn.inventory.entity.Inventory;
import com.dawn.inventory.entity.InventoryTransaction;
import com.dawn.inventory.infra.persistence.JpaInventoryRepository;
import com.dawn.inventory.infra.persistence.JpaInventoryTransactionRepository;
import com.dawn.inventory.port.in.QueryInventoryUseCase;
import com.dawn.inventory.port.out.InventoryCachePort;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryQueryService implements QueryInventoryUseCase {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final JpaInventoryRepository inventoryRepository;
    private final JpaInventoryTransactionRepository transactionRepository;
    private final InventoryCachePort cachePort;

    @Override
    @Transactional(readOnly = true)
    public ResponsePage<InventoryDto> getAll(int page, int size, Long skuId, boolean lowStock) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Specification<Inventory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (skuId != null) {
                predicates.add(cb.equal(root.get("skuId"), skuId));
            }
            if (lowStock) {
                predicates.add(cb.lessThanOrEqualTo(
                        cb.diff(root.get("stockQuantity"), root.get("reservedQuantity")),
                        LOW_STOCK_THRESHOLD));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return ResponsePage.of(inventoryRepository.findAll(spec, pageable).map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto getBySkuId(Long skuId) {
        Inventory inv = inventoryRepository.findBySkuId(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + skuId));
        return toDto(inv);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponsePage<InventoryTransactionDto> getTransactions(Long skuId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<InventoryTransaction> result = transactionRepository.findBySkuId(skuId, pageable);
        return ResponsePage.of(result.map(this::toTransactionDto));
    }

    @Override
    public Integer getAvailableQuantity(Long skuId) {
        // Redis first
        Integer cached = cachePort.getAvailable(skuId);
        if (cached != null) return cached;

        // Fallback DB
        return inventoryRepository.findBySkuId(skuId)
                .map(Inventory::getAvailableQuantity)
                .orElse(null);
    }


    // Mappers
    private InventoryDto toDto(Inventory inv) {
        return new InventoryDto(
                inv.getSkuId(),
                null,
                inv.getStockQuantity(),
                inv.getReservedQuantity(),
                inv.getAvailableQuantity());
    }

    private InventoryTransactionDto toTransactionDto(InventoryTransaction t) {
        return new InventoryTransactionDto(
                t.getId(),
                t.getSkuId(),
                t.getType(),
                t.getQuantity(),
                t.getUnitCost(),
                t.getReferenceType(),
                t.getReferenceId(),
                t.getCreatedBy(),
                t.getCreatedAt()
        );
    }
}
