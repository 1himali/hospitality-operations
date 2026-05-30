package com.hospitality.operations.dashboard.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hospitality.operations.domain.inventory.InventoryItem;
import com.hospitality.operations.domain.inventory.InventoryItemRepository;
import com.hospitality.operations.domain.inventory.InventoryStatus;
import com.hospitality.operations.domain.inventory.InventoryType;
import com.hospitality.operations.domain.restaurant.billing.Bill;
import com.hospitality.operations.domain.restaurant.billing.BillRepository;
import com.hospitality.operations.domain.restaurant.billing.BillStatus;
import com.hospitality.operations.domain.restaurant.table.DiningTable;
import com.hospitality.operations.domain.restaurant.table.DiningTableRepository;
import com.hospitality.operations.domain.restaurant.table.TableStatus;
import com.hospitality.operations.domain.restaurant.table.TableType;
import com.hospitality.operations.domain.room.Room;
import com.hospitality.operations.domain.room.RoomRepository;
import com.hospitality.operations.domain.room.RoomStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final BillRepository billRepository;
    private final RoomRepository roomRepository;
    private final DiningTableRepository diningTableRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public Map<String, Object> getAnalytics() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("revenue", getRevenueAnalytics());
        result.put("invoices", getInvoiceAnalytics());
        result.put("rooms", getRoomAnalytics());
        result.put("tables", getTableAnalytics());
        result.put("billing", getBillingAnalytics());
        result.put("inventory", getInventoryAnalytics());
        return result;
    }

    private Map<String, Object> getRevenueAnalytics() {
        List<Bill> bills = billRepository.findAll();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal paidRevenue = BigDecimal.ZERO;
        BigDecimal unpaidRevenue = BigDecimal.ZERO;
        int revenueBillCount = 0;

        for (Bill bill : bills) {
            if (bill.getStatus() == BillStatus.PAID || bill.getStatus() == BillStatus.UNPAID) {
                BigDecimal due = bill.getTotalDue() != null ? bill.getTotalDue() : BigDecimal.ZERO;
                totalRevenue = totalRevenue.add(due);
                revenueBillCount++;
                if (bill.getStatus() == BillStatus.PAID) {
                    paidRevenue = paidRevenue.add(due);
                } else {
                    unpaidRevenue = unpaidRevenue.add(due);
                }
            }
        }

        BigDecimal averageOrderValue = revenueBillCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(revenueBillCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return Map.of(
                "totalRevenue", totalRevenue,
                "paidRevenue", paidRevenue,
                "unpaidRevenue", unpaidRevenue,
                "averageOrderValue", averageOrderValue
        );
    }

    private Map<String, Object> getInvoiceAnalytics() {
        List<Bill> bills = billRepository.findAll();
        int totalInvoices = bills.size();
        int paidInvoices = 0;
        int unpaidInvoices = 0;
        int cancelledInvoices = 0;
        int voidInvoices = 0;
        int refundedInvoices = 0;

        for (Bill bill : bills) {
            switch (bill.getStatus()) {
                case PAID -> paidInvoices++;
                case UNPAID -> unpaidInvoices++;
                case CANCELLED -> cancelledInvoices++;
                case VOID -> voidInvoices++;
                case REFUNDED -> refundedInvoices++;
            }
        }

        return Map.of(
                "totalInvoices", totalInvoices,
                "paidInvoices", paidInvoices,
                "unpaidInvoices", unpaidInvoices,
                "cancelledInvoices", cancelledInvoices,
                "voidInvoices", voidInvoices,
                "refundedInvoices", refundedInvoices
        );
    }

    private Map<String, Object> getRoomAnalytics() {
        List<Room> rooms = roomRepository.findAll();
        int totalRooms = rooms.size();
        int vacantRooms = 0;
        int occupiedRooms = 0;
        int reservedRooms = 0;
        int underMaintenanceRooms = 0;

        for (Room room : rooms) {
            switch (room.getStatus()) {
                case VACANT -> vacantRooms++;
                case OCCUPIED -> occupiedRooms++;
                case RESERVED -> reservedRooms++;
                case UNDER_MAINTENANCE -> underMaintenanceRooms++;
            }
        }

        double occupancyRate = totalRooms > 0
                ? (double) occupiedRooms / totalRooms
                : 0.0;

        Map<String, Long> roomsByType = rooms.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getType().name(),
                        Collectors.counting()
                ));

        Map<String, Object> result = new HashMap<>();
        result.put("totalRooms", totalRooms);
        result.put("vacantRooms", vacantRooms);
        result.put("occupiedRooms", occupiedRooms);
        result.put("reservedRooms", reservedRooms);
        result.put("underMaintenanceRooms", underMaintenanceRooms);
        result.put("occupancyRate", occupancyRate);
        result.put("byType", roomsByType);
        return result;
    }

    private Map<String, Object> getTableAnalytics() {
        List<DiningTable> tables = diningTableRepository.findAll();
        int totalTables = tables.size();
        int availableTables = 0;
        int occupiedTables = 0;
        int reservedTables = 0;

        for (DiningTable table : tables) {
            switch (table.getStatus()) {
                case AVAILABLE -> availableTables++;
                case OCCUPIED -> occupiedTables++;
                case RESERVED -> reservedTables++;
            }
        }

        double utilizationRate = totalTables > 0
                ? (double) (occupiedTables + reservedTables) / totalTables
                : 0.0;

        Map<String, Long> tablesByType = tables.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTableType().name(),
                        Collectors.counting()
                ));

        Map<String, Object> result = new HashMap<>();
        result.put("totalTables", totalTables);
        result.put("availableTables", availableTables);
        result.put("occupiedTables", occupiedTables);
        result.put("reservedTables", reservedTables);
        result.put("utilizationRate", utilizationRate);
        result.put("byType", tablesByType);
        return result;
    }

    private Map<String, Object> getBillingAnalytics() {
        List<Bill> bills = billRepository.findAll();
        BigDecimal totalTaxCollected = BigDecimal.ZERO;
        BigDecimal totalDiscountsGiven = BigDecimal.ZERO;
        BigDecimal totalDue = BigDecimal.ZERO;
        int countableBills = 0;

        for (Bill bill : bills) {
            if (bill.getStatus() == BillStatus.PAID || bill.getStatus() == BillStatus.UNPAID) {
                totalTaxCollected = totalTaxCollected.add(
                        bill.getTaxAmount() != null ? bill.getTaxAmount() : BigDecimal.ZERO);
                totalDiscountsGiven = totalDiscountsGiven.add(
                        bill.getDiscount() != null ? bill.getDiscount() : BigDecimal.ZERO);
                totalDue = totalDue.add(
                        bill.getTotalDue() != null ? bill.getTotalDue() : BigDecimal.ZERO);
                countableBills++;
            }
        }

        BigDecimal averageBillAmount = countableBills > 0
                ? totalDue.divide(BigDecimal.valueOf(countableBills), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return Map.of(
                "totalBills", bills.size(),
                "countableBills", countableBills,
                "totalTaxCollected", totalTaxCollected,
                "totalDiscountsGiven", totalDiscountsGiven,
                "averageBillAmount", averageBillAmount
        );
    }

    private Map<String, Object> getInventoryAnalytics() {
        List<InventoryItem> items = inventoryItemRepository.findAll();
        int totalItems = items.size();
        int inStockItems = 0;
        int lowStockItems = 0;
        int outOfStockItems = 0;
        int discontinuedItems = 0;

        for (InventoryItem item : items) {
            switch (item.getStatus()) {
                case IN_STOCK -> inStockItems++;
                case LOW_STOCK -> lowStockItems++;
                case OUT_OF_STOCK -> outOfStockItems++;
                case DISCONTINUED -> discontinuedItems++;
            }
        }

        long lodgingItems = items.stream().filter(i -> i.getType() == InventoryType.LODGING).count();
        long restaurantItems = items.stream().filter(i -> i.getType() == InventoryType.RESTAURANT).count();
        long itemsBelowReorderLevel = items.stream()
                .filter(i -> i.getQuantity() < i.getReorderLevel())
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("totalItems", totalItems);
        result.put("inStockItems", inStockItems);
        result.put("lowStockItems", lowStockItems);
        result.put("outOfStockItems", outOfStockItems);
        result.put("discontinuedItems", discontinuedItems);
        result.put("lodgingItems", lodgingItems);
        result.put("restaurantItems", restaurantItems);
        result.put("itemsBelowReorderLevel", itemsBelowReorderLevel);
        return result;
    }
}
