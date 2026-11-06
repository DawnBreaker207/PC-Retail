package com.dawn.order.infra.persistence;

import com.dawn.order.entity.WarrantyItem;
import com.dawn.order.port.out.WarrantyItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaWarrantyItemRepository extends JpaRepository<WarrantyItem, Long>, WarrantyItemRepository {

    @Override
    List<WarrantyItem> findBySubOrderId(Long subOrderId);

    @Override
    List<WarrantyItem> findBySkuId(Long skuId);
}
