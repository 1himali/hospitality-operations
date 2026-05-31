package com.hospitality.operations.domain.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hospitality.operations.domain.inventory.dto.InventoryItemRequestDto;
import com.hospitality.operations.domain.inventory.dto.InventoryItemResponseDto;
import com.hospitality.operations.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class InventoryItemServiceImplTest {

    @Mock
    private InventoryItemRepository repository;

    private InventoryItemServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InventoryItemServiceImpl(repository);
    }

    @Test
    void createInventoryItem_shouldCreateItem() {
        InventoryItemRequestDto request = InventoryItemRequestDto.builder()
                .name("Bath Towel").type(InventoryType.LODGING).category("LINEN")
                .quantity(50).reorderLevel(10).unit("pcs").build();

        InventoryItem saved = InventoryItem.builder()
                .id(1L).name("Bath Towel").type(InventoryType.LODGING).category("LINEN")
                .quantity(50).reorderLevel(10).unit("pcs").status(InventoryStatus.IN_STOCK)
                .tenantSchema("default").build();

        when(repository.save(any(InventoryItem.class))).thenReturn(saved);

        InventoryItemResponseDto result = service.createInventoryItem(request);

        assertNotNull(result);
        assertEquals("Bath Towel", result.getName());
        assertEquals(InventoryType.LODGING, result.getType());
        assertEquals(InventoryStatus.IN_STOCK, result.getStatus());
        verify(repository).save(any(InventoryItem.class));
    }

    @Test
    void createInventoryItem_shouldSetLowStockWhenQuantityBelowReorder() {
        InventoryItemRequestDto request = InventoryItemRequestDto.builder()
                .name("Tomato Sauce").type(InventoryType.RESTAURANT).category("INGREDIENT")
                .quantity(2).reorderLevel(10).unit("kg").build();

        InventoryItem saved = InventoryItem.builder()
                .id(2L).name("Tomato Sauce").type(InventoryType.RESTAURANT).category("INGREDIENT")
                .quantity(2).reorderLevel(10).unit("kg").status(InventoryStatus.LOW_STOCK)
                .tenantSchema("default").build();

        when(repository.save(any(InventoryItem.class))).thenReturn(saved);

        InventoryItemResponseDto result = service.createInventoryItem(request);

        assertEquals(InventoryStatus.LOW_STOCK, result.getStatus());
    }

    @Test
    void createInventoryItem_shouldSetOutOfStockWhenQuantityZero() {
        InventoryItemRequestDto request = InventoryItemRequestDto.builder()
                .name("Discontinued Item").type(InventoryType.LODGING).category("OTHER")
                .quantity(0).reorderLevel(5).unit("pcs").build();

        InventoryItem saved = InventoryItem.builder()
                .id(3L).name("Discontinued Item").type(InventoryType.LODGING).category("OTHER")
                .quantity(0).reorderLevel(5).unit("pcs").status(InventoryStatus.OUT_OF_STOCK)
                .tenantSchema("default").build();

        when(repository.save(any(InventoryItem.class))).thenReturn(saved);

        InventoryItemResponseDto result = service.createInventoryItem(request);

        assertEquals(InventoryStatus.OUT_OF_STOCK, result.getStatus());
    }

    @Test
    void getById_shouldReturnItem() {
        InventoryItem item = InventoryItem.builder()
                .id(1L).name("Bath Towel").type(InventoryType.LODGING).category("LINEN")
                .quantity(50).reorderLevel(10).unit("pcs").status(InventoryStatus.IN_STOCK)
                .tenantSchema("default").build();

        when(repository.findById(1L)).thenReturn(Optional.of(item));

        InventoryItemResponseDto result = service.getInventoryItemById(1L);

        assertNotNull(result);
        assertEquals("Bath Towel", result.getName());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getInventoryItemById(999L));
    }

    @Test
    void getAll_shouldReturnAllItems() {
        when(repository.findAll()).thenReturn(List.of(
                InventoryItem.builder().id(1L).name("Item A").type(InventoryType.LODGING).category("LINEN")
                        .quantity(10).reorderLevel(5).unit("pcs").status(InventoryStatus.IN_STOCK)
                        .tenantSchema("default").build(),
                InventoryItem.builder().id(2L).name("Item B").type(InventoryType.RESTAURANT).category("INGREDIENT")
                        .quantity(20).reorderLevel(5).unit("kg").status(InventoryStatus.IN_STOCK)
                        .tenantSchema("default").build()
        ));

        List<InventoryItemResponseDto> results = service.getAllInventoryItems();

        assertEquals(2, results.size());
    }

    @Test
    void getByType_shouldFilterByType() {
        when(repository.findByType(InventoryType.LODGING)).thenReturn(List.of(
                InventoryItem.builder().id(1L).name("Linen Item").type(InventoryType.LODGING).category("LINEN")
                        .quantity(10).reorderLevel(5).unit("pcs").status(InventoryStatus.IN_STOCK)
                        .tenantSchema("default").build()
        ));

        List<InventoryItemResponseDto> results = service.getInventoryItemsByType(InventoryType.LODGING);

        assertEquals(1, results.size());
        assertEquals("Linen Item", results.get(0).getName());
    }

    @Test
    void updateQuantity_shouldIncreaseStock() {
        InventoryItem item = InventoryItem.builder()
                .id(1L).name("Bath Towel").type(InventoryType.LODGING).category("LINEN")
                .quantity(10).reorderLevel(5).unit("pcs").status(InventoryStatus.IN_STOCK)
                .tenantSchema("default").build();

        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(repository.save(any(InventoryItem.class))).thenAnswer(i -> i.getArgument(0));

        InventoryItemResponseDto result = service.updateQuantity(1L, 5);

        assertEquals(15, result.getQuantity());
        assertEquals(InventoryStatus.IN_STOCK, result.getStatus());
    }

    @Test
    void updateQuantity_shouldDecreaseStockAndChangeStatus() {
        InventoryItem item = InventoryItem.builder()
                .id(1L).name("Bath Towel").type(InventoryType.LODGING).category("LINEN")
                .quantity(3).reorderLevel(5).unit("pcs").status(InventoryStatus.IN_STOCK)
                .tenantSchema("default").build();

        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(repository.save(any(InventoryItem.class))).thenAnswer(i -> i.getArgument(0));

        InventoryItemResponseDto result = service.updateQuantity(1L, -2);

        assertEquals(1, result.getQuantity());
        assertEquals(InventoryStatus.LOW_STOCK, result.getStatus());
    }

    @Test
    void updateQuantity_shouldNotGoBelowZero() {
        InventoryItem item = InventoryItem.builder()
                .id(1L).name("Item").type(InventoryType.LODGING).category("OTHER")
                .quantity(2).reorderLevel(5).unit("pcs").status(InventoryStatus.LOW_STOCK)
                .tenantSchema("default").build();

        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(repository.save(any(InventoryItem.class))).thenAnswer(i -> i.getArgument(0));

        InventoryItemResponseDto result = service.updateQuantity(1L, -10);

        assertEquals(0, result.getQuantity());
        assertEquals(InventoryStatus.OUT_OF_STOCK, result.getStatus());
    }

    @Test
    void setStatus_shouldUpdateStatus() {
        InventoryItem item = InventoryItem.builder()
                .id(1L).name("Item").type(InventoryType.LODGING).category("OTHER")
                .quantity(10).reorderLevel(5).unit("pcs").status(InventoryStatus.IN_STOCK)
                .tenantSchema("default").build();

        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(repository.save(any(InventoryItem.class))).thenAnswer(i -> i.getArgument(0));

        InventoryItemResponseDto result = service.setStatus(1L, InventoryStatus.DISCONTINUED);

        assertEquals(InventoryStatus.DISCONTINUED, result.getStatus());
    }

    @Test
    void deleteItem_shouldDiscontinue() {
        InventoryItem item = InventoryItem.builder()
                .id(1L).name("Item").type(InventoryType.LODGING).category("OTHER")
                .quantity(10).reorderLevel(5).unit("pcs").status(InventoryStatus.IN_STOCK)
                .tenantSchema("default").build();

        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(repository.save(any(InventoryItem.class))).thenAnswer(i -> i.getArgument(0));

        InventoryItemResponseDto result = service.deleteInventoryItem(1L);

        assertEquals(InventoryStatus.DISCONTINUED, result.getStatus());
        verify(repository).save(item);
    }

    @Test
    void deleteItem_shouldThrowWhenNotFound() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteInventoryItem(999L));
    }
}
