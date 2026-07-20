package com.dawn.cart.service;

import com.dawn.cart.dto.CartSummaryDto;
import com.dawn.cart.entity.CartItem;
import com.dawn.cart.infra.persistence.JpaCartItemRepository;
import com.dawn.cart.port.out.CartCachePort;
import com.dawn.cart.port.out.InventoryQueryPort;
import com.dawn.catalog.dto.CatalogQueryPort;
import com.dawn.catalog.dto.SkuPriceInfo;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {
    @Mock
    JpaCartItemRepository cartItemRepository;
    @Mock
    CartCachePort cartCachePort;
    @Mock
    CatalogQueryPort catalogQueryPort;
    @Mock
    InventoryQueryPort inventoryQueryPort;

    @InjectMocks
    CartService cartService;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }


    // getCart
    @Nested
    @DisplayName("getCart - cache-aside behavior")
    class GetCart {

        @Test
        @DisplayName("Cache hit -> return from cache without touching DB")
        void cacheHit_returnsFromCache_withoutDbCall() {
            CartSummaryDto cached = new CartSummaryDto(List.of(), 0, BigDecimal.ZERO);
            when(cartCachePort.getCart(1L)).thenReturn(Optional.of(cached));

            CartSummaryDto result = cartService.getCart(1L);

            assertThat(result).isSameAs(cached);
            verifyNoInteractions(cartItemRepository);
        }

        @Test
        @DisplayName("Cache miss + empty cart -> return empty, do not populate cache")
        void cacheMiss_emptyCart_returnsEmpty() {
            when(cartCachePort.getCart(1L)).thenReturn(Optional.empty());
            when(cartItemRepository.findByUserId(1L)).thenReturn(List.of());

            CartSummaryDto result = cartService.getCart(1L);

            assertThat(result.items()).isEmpty();
            assertThat(result.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            verify(cartCachePort, never()).putCart(anyLong(), any());
        }

        @Test
        @DisplayName("Cache miss + items exist -> rebuild from DB and populate cache")
        void cacheMiss_withItems_rebuildsAndCaches() {
            CartItem item = buildCartItem(1L, 10L, 2);
            SkuPriceInfo priceInfo = new SkuPriceInfo(10L, "Intel CPU", "Core i5", BigDecimal.valueOf(5_000_000));

            when(cartCachePort.getCart(1L)).thenReturn(Optional.empty());
            when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(item));
            when(catalogQueryPort.getSkuDetailsMap(List.of(10L))).thenReturn(Map.of(10L, priceInfo));

            CartSummaryDto result = cartService.getCart(1L);

            assertThat(result.items()).hasSize(1);
            assertThat(result.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(10_000_000));
            verify(cartCachePort).putCart(eq(1L), any(CartSummaryDto.class));
        }

        @Test
        @DisplayName("SKU no longer in catalog -> unitPrice defaults to zero without throwing")
        void skuNotInCatalog_usesZeroPrice() {
            CartItem item = buildCartItem(1L, 99L, 1);

            when(cartCachePort.getCart(1L)).thenReturn(Optional.empty());
            when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(item));
            when(catalogQueryPort.getSkuDetailsMap(List.of(99L))).thenReturn(Map.of());

            CartSummaryDto result = cartService.getCart(1L);

            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).unitPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // addItem
    @Nested
    @DisplayName("addItem")
    class AddItem {
        @Test
        @DisplayName("Item not in cart -> create new entry")
        void itemNotExists_createsNew() {
            when(inventoryQueryPort.getAvailableQuantity(10L)).thenReturn(10);
            when(cartItemRepository.findByUserIdAndSkuId(1L, 10L)).thenReturn(Optional.empty());
            when(cartItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            cartService.addItem(1L, 10L, 2);

            verify(cartItemRepository).save(argThat(i -> i.getQuantity() == 2 && i.getSkuId().equals(10L)));
        }

        @Test
        @DisplayName("Item already in cart -> accumulate quantity")
        void itemExists_addsQuantity() {
            when(inventoryQueryPort.getAvailableQuantity(10L)).thenReturn(10);
            CartItem existing = buildCartItem(1L, 10L, 3);
            when(cartItemRepository.findByUserIdAndSkuId(1L, 10L)).thenReturn(Optional.of(existing));
            when(cartItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            cartService.addItem(1L, 10L, 2);

            verify(cartItemRepository).save(argThat(i -> i.getQuantity() == 5));
        }

        @Test
        @DisplayName("After save -> cache must be evicted")
        void afterSave_cacheEvicted() {
            when(inventoryQueryPort.getAvailableQuantity(anyLong())).thenReturn(10);
            when(cartItemRepository.findByUserIdAndSkuId(anyLong(), anyLong())).thenReturn(Optional.empty());
            when(cartItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            cartService.addItem(1L, 10L, 1);
        }
    }


    // updateItem
    @Nested
    @DisplayName("updateItem")
    class UpdateItem {
        @Test
        @DisplayName("Item exists -> update quantity and evict cache")
        void itemExists_updatesAndEvicts() {
            CartItem item = buildCartItem(1L, 10L, 3);
            when(cartItemRepository.findByUserIdAndSkuId(1L, 10L)).thenReturn(Optional.of(item));
            when(cartItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            cartService.updateItem(1L, 10L, 5);

            verify(cartItemRepository).save(argThat(i -> i.getQuantity() == 5));
        }

        @Test
        @DisplayName("Item not found -> throw ResourceNotFoundException")
        void itemNotExists_throws() {
            when(cartItemRepository.findByUserIdAndSkuId(1L, 10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.updateItem(1L, 10L, 5))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // removeItem
    @Nested
    @DisplayName("removeItem")
    class RemoveItem {
        @Test
        @DisplayName("Remove item -> delete from DB and evict cache")
        void removeItem_deletesAndEvicts() {
            cartService.removeItem(1L, 10L);

            verify(cartItemRepository).deleteByUserIdAndSkuId(1L, 10L);
        }
    }

    // getItemCount - cache-aside
    @Nested
    @DisplayName("getItemCount - cache-aside")
    class GetItemCount {
        @Test
        @DisplayName("Cache hit -> return from cache without querying DB")
        void cacheHit_returnsFromCache() {
            when(cartCachePort.getItemCount(1L)).thenReturn(Optional.of(3));

            int count = cartService.getItemCount(1L);

            assertThat(count).isEqualTo(3);
            verifyNoInteractions(cartItemRepository);
        }

        @Test
        @DisplayName("Cache miss -> query DB and store in cache")
        void cacheMiss_queriesDbAndCaches() {
            when(cartCachePort.getItemCount(1L)).thenReturn(Optional.empty());
            when(cartItemRepository.countByUserId(1L)).thenReturn(4);

            int count = cartService.getItemCount(1L);

            assertThat(count).isEqualTo(4);
            verify(cartCachePort).putItemCount(1L, 4);
        }
    }

    // Helpers
    private CartItem buildCartItem(Long userId, Long skuId, int quantity) {
        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setSkuId(skuId);
        item.setQuantity(quantity);
        return item;
    }
}
