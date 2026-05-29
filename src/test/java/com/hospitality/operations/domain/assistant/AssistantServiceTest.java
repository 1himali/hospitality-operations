package com.hospitality.operations.domain.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hospitality.operations.domain.assistant.dto.AssistantQueryDto;
import com.hospitality.operations.domain.assistant.dto.AssistantResponseDto;
import com.hospitality.operations.domain.inventory.InventoryItemService;
import com.hospitality.operations.domain.inventory.InventoryStatus;
import com.hospitality.operations.domain.inventory.InventoryType;
import com.hospitality.operations.domain.inventory.dto.InventoryItemResponseDto;
import com.hospitality.operations.domain.restaurant.order.RestaurantOrderService;
import com.hospitality.operations.domain.restaurant.order.dto.OrderResponseDto;
import com.hospitality.operations.domain.restaurant.table.DiningTableService;
import com.hospitality.operations.domain.restaurant.table.TableStatus;
import com.hospitality.operations.domain.restaurant.table.dto.TableResponseDto;
import com.hospitality.operations.domain.room.RoomService;
import com.hospitality.operations.domain.room.RoomStatus;
import com.hospitality.operations.domain.room.dto.RoomResponseDto;
import com.hospitality.operations.lodging.actionitems.ActionItemService;
import com.hospitality.operations.lodging.actionitems.ActionItemCategory;
import com.hospitality.operations.lodging.actionitems.ActionItemStatus;
import com.hospitality.operations.lodging.actionitems.dto.ActionItemResponseDto;

@ExtendWith(MockitoExtension.class)
class AssistantServiceTest {

    @Mock
    private RestaurantOrderService orderService;

    @Mock
    private RoomService roomService;

    @Mock
    private DiningTableService tableService;

    @Mock
    private InventoryItemService inventoryService;

    @Mock
    private ActionItemService actionItemService;

    private AssistantService assistantService;

    @BeforeEach
    void setUp() {
        assistantService = new AssistantService(orderService, roomService, tableService, inventoryService, actionItemService);
    }

    @Test
    void processQuery_unknownQuery_returnsNotAvailable() {
        AssistantResponseDto res = assistantService.processQuery(new AssistantQueryDto("what is the meaning of life"));

        assertEquals("Information not available in the system.", res.getResponseText());
        assertNull(res.getSource());
    }

    @Test
    void processQuery_orderQuery_returnsOrderSummary() {
        OrderResponseDto order = OrderResponseDto.builder()
                .id(1L).orderReference("ORD-001").status("NEW").tableNumber("T1")
                .totalAmount(new BigDecimal("150.00")).build();

        when(orderService.getAllOrders(null, null, null)).thenReturn(List.of(order));

        AssistantResponseDto res = assistantService.processQuery(new AssistantQueryDto("show my orders"));

        assertEquals("orders", res.getSource());
        assertEquals("Total orders: 1 | Active: 1 | Completed: 0 | Cancelled: 0", res.getResponseText());
    }

    @Test
    void processQuery_orderQueryWithActive_returnsActiveDetails() {
        OrderResponseDto active = OrderResponseDto.builder()
                .id(1L).orderReference("ORD-001").status("PREPARING").tableNumber("T1")
                .totalAmount(new BigDecimal("150.00")).build();
        OrderResponseDto completed = OrderResponseDto.builder()
                .id(2L).orderReference("ORD-002").status("COMPLETED").tableNumber("T2")
                .totalAmount(new BigDecimal("200.00")).build();

        when(orderService.getAllOrders(null, null, null)).thenReturn(List.of(active, completed));

        AssistantResponseDto res = assistantService.processQuery(new AssistantQueryDto("pending orders"));

        assertEquals("orders", res.getSource());
        assertEquals("Total orders: 2 | Active: 1 | Completed: 1 | Cancelled: 0\n\nActive orders:\nORD-001 — PREPARING — Table T1 (₹150)", res.getResponseText());
    }

    @Test
    void processQuery_roomQuery_returnsRoomSummary() {
        RoomResponseDto room = RoomResponseDto.builder()
                .id(1L).roomNumber("101").status(RoomStatus.VACANT).ratePerNight(new BigDecimal("250.00"))
                .build();

        when(roomService.getAllRooms()).thenReturn(List.of(room));

        AssistantResponseDto res = assistantService.processQuery(new AssistantQueryDto("room status"));

        assertEquals("rooms", res.getSource());
        assertEquals("Total rooms: 1 | Vacant: 1 | Occupied: 0 | Under maintenance: 0 | Reserved: 0", res.getResponseText());
    }

    @Test
    void processQuery_tableQuery_returnsTableSummary() {
        TableResponseDto table = TableResponseDto.builder()
                .id(1L).tableNumber("T1").capacity(4).status(TableStatus.AVAILABLE)
                .build();

        when(tableService.getAllTables()).thenReturn(List.of(table));

        AssistantResponseDto res = assistantService.processQuery(new AssistantQueryDto("dining tables"));

        assertEquals("tables", res.getSource());
        assertEquals("Total tables: 1 | Available: 1 | Occupied: 0 | Reserved: 0", res.getResponseText());
    }

    @Test
    void processQuery_inventoryQuery_returnsInventorySummary() {
        InventoryItemResponseDto item = InventoryItemResponseDto.builder()
                .id(1L).name("Soap").type(InventoryType.LODGING).category("CONSUMABLE")
                .quantity(50).reorderLevel(10).unit("pcs").status(InventoryStatus.IN_STOCK)
                .build();

        when(inventoryService.getAllInventoryItems()).thenReturn(List.of(item));

        AssistantResponseDto res = assistantService.processQuery(new AssistantQueryDto("inventory stock"));

        assertEquals("inventory", res.getSource());
        assertEquals("Total items: 1 | Lodging: 1 | Restaurant: 0 | Low/out of stock: 0", res.getResponseText());
    }

    @Test
    void processQuery_taskQuery_returnsTaskSummary() {
        ActionItemResponseDto task = ActionItemResponseDto.builder()
                .id(1L).title("Clean room 101").category(ActionItemCategory.CLEANING).status(ActionItemStatus.TODO)
                .build();

        when(actionItemService.getAllActionItems()).thenReturn(List.of(task));

        AssistantResponseDto res = assistantService.processQuery(new AssistantQueryDto("pending action items"));

        assertEquals("tasks", res.getSource());
        assertEquals("Total tasks: 1 | To do: 1 | Completed: 0\n\nPending tasks:\n• Clean room 101 — CLEANING", res.getResponseText());
    }

    @Test
    void processQuery_emptyData_returnsEmptySummary() {
        when(orderService.getAllOrders(null, null, null)).thenReturn(List.of());

        AssistantResponseDto res = assistantService.processQuery(new AssistantQueryDto("orders"));

        assertEquals("No orders found in the system.", res.getResponseText());
        assertEquals("orders", res.getSource());
    }
}
