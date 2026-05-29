package com.hospitality.operations.domain.assistant;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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
import com.hospitality.operations.lodging.actionitems.ActionItemStatus;
import com.hospitality.operations.lodging.actionitems.dto.ActionItemResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssistantService {

    private static final String NOT_AVAILABLE = "Information not available in the system.";

    private final RestaurantOrderService orderService;
    private final RoomService roomService;
    private final DiningTableService tableService;
    private final InventoryItemService inventoryService;
    private final ActionItemService actionItemService;

    public AssistantResponseDto processQuery(AssistantQueryDto dto) {
        String query = dto.getQuery().trim().toLowerCase();

        if (matches(query, "order", "orders", "pending order", "active order")) {
            return handleOrderQuery(query);
        }
        if (matches(query, "room", "rooms", "reservation", "reservations", "occupied", "vacant")) {
            return handleRoomQuery(query);
        }
        if (matches(query, "table", "tables", "dining")) {
            return handleTableQuery(query);
        }
        if (matches(query, "task", "tasks", "action item", "action items", "pending task", "to-do", "todo")) {
            return handleTaskQuery(query);
        }
        if (matches(query, "inventory", "stock", "supply", "supplies")) {
            return handleInventoryQuery(query);
        }

        return AssistantResponseDto.builder()
                .responseText(NOT_AVAILABLE)
                .source(null)
                .build();
    }

    private AssistantResponseDto handleOrderQuery(String query) {
        List<OrderResponseDto> allOrders = orderService.getAllOrders(null, null, null);
        if (allOrders.isEmpty()) {
            return response("No orders found in the system.", "orders");
        }
        long active = allOrders.stream().filter(o -> !"COMPLETED".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus())).count();
        long completed = allOrders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count();
        long cancelled = allOrders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();
        String summary = String.format("Total orders: %d | Active: %d | Completed: %d | Cancelled: %d", allOrders.size(), active, completed, cancelled);

        if (matches(query, "pending", "active", "new", "preparing", "ready")) {
            List<OrderResponseDto> activeOrders = allOrders.stream()
                    .filter(o -> !"COMPLETED".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus()))
                    .toList();
            if (activeOrders.isEmpty()) {
                return response("No active orders. " + summary, "orders");
            }
            String details = activeOrders.stream()
                    .limit(5)
                    .map(o -> String.format("%s — %s — %s (₹%.0f)",
                            o.getOrderReference() != null ? o.getOrderReference() : "ORD#" + o.getId(),
                            o.getStatus(), o.getTableNumber() != null ? "Table " + o.getTableNumber() : "—",
                            o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0))
                    .collect(Collectors.joining("\n"));
            String extra = activeOrders.size() > 5 ? "\n... and " + (activeOrders.size() - 5) + " more" : "";
            return response(summary + "\n\nActive orders:\n" + details + extra, "orders");
        }
        return response(summary, "orders");
    }

    private AssistantResponseDto handleRoomQuery(String query) {
        List<RoomResponseDto> allRooms = roomService.getAllRooms();
        if (allRooms.isEmpty()) {
            return response("No rooms found in the system.", "rooms");
        }
        long vacant = allRooms.stream().filter(r -> RoomStatus.VACANT == r.getStatus()).count();
        long occupied = allRooms.stream().filter(r -> RoomStatus.OCCUPIED == r.getStatus()).count();
        long maintenance = allRooms.stream().filter(r -> RoomStatus.UNDER_MAINTENANCE == r.getStatus()).count();
        long reserved = allRooms.stream().filter(r -> RoomStatus.RESERVED == r.getStatus()).count();
        String summary = String.format("Total rooms: %d | Vacant: %d | Occupied: %d | Under maintenance: %d | Reserved: %d",
                allRooms.size(), vacant, occupied, maintenance, reserved);

        if (matches(query, "vacant", "available", "empty")) {
            List<RoomResponseDto> vacantRooms = allRooms.stream()
                    .filter(r -> RoomStatus.VACANT == r.getStatus()).toList();
            String details = vacantRooms.isEmpty() ? "None" : vacantRooms.stream()
                    .limit(10).map(r -> String.format("RM %s — %s — ₹%.0f/night",
                            r.getRoomNumber(), fmtType(r.getType().name()), r.getRatePerNight().doubleValue()))
                    .collect(Collectors.joining("\n"));
            return response("Vacant rooms (" + vacantRooms.size() + "):\n" + details, "rooms");
        }
        if (matches(query, "occupied")) {
            long occCount = allRooms.stream().filter(r -> RoomStatus.OCCUPIED == r.getStatus()).count();
            return response("Occupied rooms: " + occCount + " / " + allRooms.size() + "\n" + summary, "rooms");
        }
        return response(summary, "rooms");
    }

    private AssistantResponseDto handleTableQuery(String query) {
        List<TableResponseDto> allTables = tableService.getAllTables();
        if (allTables.isEmpty()) {
            return response("No tables found in the system.", "tables");
        }
        long available = allTables.stream().filter(t -> TableStatus.AVAILABLE == t.getStatus()).count();
        long occupied = allTables.stream().filter(t -> TableStatus.OCCUPIED == t.getStatus()).count();
        long reserved = allTables.stream().filter(t -> TableStatus.RESERVED == t.getStatus()).count();
        String summary = String.format("Total tables: %d | Available: %d | Occupied: %d | Reserved: %d",
                allTables.size(), available, occupied, reserved);

        if (matches(query, "available", "free", "empty")) {
            List<TableResponseDto> free = allTables.stream()
                    .filter(t -> TableStatus.AVAILABLE == t.getStatus()).toList();
            String details = free.isEmpty() ? "None" : free.stream()
                    .limit(10).map(t -> String.format("Table %s — %d seats", t.getTableNumber(), t.getCapacity()))
                    .collect(Collectors.joining("\n"));
            return response("Available tables (" + free.size() + "):\n" + details, "tables");
        }
        return response(summary, "tables");
    }

    private AssistantResponseDto handleInventoryQuery(String query) {
        List<InventoryItemResponseDto> allItems = inventoryService.getAllInventoryItems();
        if (allItems.isEmpty()) {
            return response("No inventory items found in the system.", "inventory");
        }
        long total = allItems.size();
        long lodging = allItems.stream().filter(i -> InventoryType.LODGING.equals(i.getType())).count();
        long restaurant = allItems.stream().filter(i -> InventoryType.RESTAURANT.equals(i.getType())).count();
        long lowStock = allItems.stream().filter(i -> InventoryStatus.LOW_STOCK == i.getStatus() || InventoryStatus.OUT_OF_STOCK == i.getStatus()).count();
        String summary = String.format("Total items: %d | Lodging: %d | Restaurant: %d | Low/out of stock: %d",
                total, lodging, restaurant, lowStock);

        if (matches(query, "low stock", "out of stock", "reorder", "restock")) {
            List<InventoryItemResponseDto> low = allItems.stream()
                    .filter(i -> InventoryStatus.LOW_STOCK == i.getStatus() || InventoryStatus.OUT_OF_STOCK == i.getStatus())
                    .toList();
            String details = low.isEmpty() ? "All items in stock." : low.stream()
                    .limit(10).map(i -> String.format("%s — %s (%d %s) — %s",
                            i.getName(), i.getCategory(), i.getQuantity(), i.getUnit(), i.getStatus().name().replace('_', ' ')))
                    .collect(Collectors.joining("\n"));
            return response(summary + "\n\nItems needing attention:\n" + details, "inventory");
        }
        return response(summary, "inventory");
    }

    private AssistantResponseDto handleTaskQuery(String query) {
        List<ActionItemResponseDto> allTasks = actionItemService.getAllActionItems();
        if (allTasks.isEmpty()) {
            return response("No tasks found in the system.", "tasks");
        }
        long todo = allTasks.stream().filter(t -> ActionItemStatus.TODO == t.getStatus()).count();
        long completed = allTasks.stream().filter(t -> ActionItemStatus.COMPLETED == t.getStatus()).count();
        String summary = String.format("Total tasks: %d | To do: %d | Completed: %d", allTasks.size(), todo, completed);

        if (matches(query, "pending", "todo", "to-do", "open", "incomplete")) {
            List<ActionItemResponseDto> pending = allTasks.stream()
                    .filter(t -> ActionItemStatus.TODO == t.getStatus()).toList();
            String details = pending.isEmpty() ? "No pending tasks." : pending.stream()
                    .limit(10).map(t -> String.format("• %s — %s%s",
                            t.getTitle(), t.getCategory(),
                            t.getDescription() != null ? " (" + t.getDescription() + ")" : ""))
                    .collect(Collectors.joining("\n"));
            String extra = pending.size() > 10 ? "\n... and " + (pending.size() - 10) + " more" : "";
            return response(summary + "\n\nPending tasks:\n" + details + extra, "tasks");
        }
        return response(summary, "tasks");
    }

    private static boolean matches(String query, String... keywords) {
        for (String kw : keywords) {
            if (query.contains(kw)) return true;
        }
        return false;
    }

    private static AssistantResponseDto response(String text, String source) {
        return AssistantResponseDto.builder().responseText(text).source(source).build();
    }

    private static String fmtType(String t) {
        if (t == null) return "";
        return switch (t) {
            case "DELUXE_KING" -> "DELUXE KING";
            case "STD_DOUBLE" -> "STD DOUBLE";
            case "SUITE" -> "SUITE";
            default -> t;
        };
    }
}
