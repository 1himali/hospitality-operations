package com.hospitality.operations.auth;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hospitality.operations.domain.inventory.InventoryItem;
import com.hospitality.operations.domain.inventory.InventoryItemRepository;
import com.hospitality.operations.domain.inventory.InventoryStatus;
import com.hospitality.operations.domain.inventory.InventoryType;
import com.hospitality.operations.domain.restaurant.billing.Bill;
import com.hospitality.operations.domain.restaurant.billing.BillLineItem;
import com.hospitality.operations.domain.restaurant.billing.BillLineItemRepository;
import com.hospitality.operations.domain.restaurant.billing.BillRepository;
import com.hospitality.operations.domain.restaurant.billing.BillStatus;
import com.hospitality.operations.domain.restaurant.menu.MenuCategory;
import com.hospitality.operations.domain.restaurant.menu.MenuItem;
import com.hospitality.operations.domain.restaurant.menu.MenuItemRepository;
import com.hospitality.operations.domain.restaurant.table.DiningTable;
import com.hospitality.operations.domain.restaurant.table.DiningTableRepository;
import com.hospitality.operations.domain.restaurant.table.TableStatus;
import com.hospitality.operations.domain.restaurant.table.TableType;
import com.hospitality.operations.domain.room.Room;
import com.hospitality.operations.domain.room.RoomRepository;
import com.hospitality.operations.domain.room.RoomStatus;
import com.hospitality.operations.domain.room.RoomType;
import com.hospitality.operations.lodging.actionitems.ActionItem;
import com.hospitality.operations.lodging.actionitems.ActionItemCategory;
import com.hospitality.operations.lodging.actionitems.ActionItemRepository;
import com.hospitality.operations.lodging.actionitems.ActionItemStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomRepository roomRepository;
    private final MenuItemRepository menuItemRepository;
    private final DiningTableRepository diningTableRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ActionItemRepository actionItemRepository;
    private final BillRepository billRepository;
    private final BillLineItemRepository billLineItemRepository;

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedRooms();
        seedMenuItems();
        seedDiningTables();
        seedInventory();
        seedActionItems();
        seedBills();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("user")) {
            userRepository.save(User.builder()
                    .username("user").passwordHash(passwordEncoder.encode("1234"))
                    .role(UserRole.ROLE_USER).tenantSchema("default").build());
        }
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin").passwordHash(passwordEncoder.encode("abcd"))
                    .role(UserRole.ROLE_ADMIN).tenantSchema("default").build());
        }
        if (!userRepository.existsByUsername("owner")) {
            userRepository.save(User.builder()
                    .username("owner").passwordHash(passwordEncoder.encode("4321"))
                    .role(UserRole.ROLE_OWNER).tenantSchema("default").build());
        }
        if (!userRepository.existsByUsername("manager")) {
            userRepository.save(User.builder()
                    .username("manager").passwordHash(passwordEncoder.encode("mngr"))
                    .role(UserRole.ROLE_MANAGER).tenantSchema("default").build());
        }
    }

    private void seedRooms() {
        if (roomRepository.count() > 0) return;
        roomRepository.saveAll(List.of(
            Room.builder().roomNumber("101").type(RoomType.STD_DOUBLE).floor(1).status(RoomStatus.VACANT).ratePerNight(new BigDecimal("1500")).build(),
            Room.builder().roomNumber("102").type(RoomType.STD_DOUBLE).floor(1).status(RoomStatus.OCCUPIED).ratePerNight(new BigDecimal("1800")).build(),
            Room.builder().roomNumber("201").type(RoomType.DELUXE_KING).floor(2).status(RoomStatus.VACANT).ratePerNight(new BigDecimal("3500")).build(),
            Room.builder().roomNumber("301").type(RoomType.SUITE).floor(3).status(RoomStatus.RESERVED).ratePerNight(new BigDecimal("5500")).build()
        ));
    }

    private void seedMenuItems() {
        if (menuItemRepository.count() > 0) return;
        menuItemRepository.saveAll(List.of(
            MenuItem.builder().name("Americano").category(MenuCategory.BEVERAGES).price(new BigDecimal("180")).available(true).build(),
            MenuItem.builder().name("Cappuccino").category(MenuCategory.BEVERAGES).price(new BigDecimal("220")).available(true).build(),
            MenuItem.builder().name("Pasta Alfredo").category(MenuCategory.MAINS).price(new BigDecimal("450")).available(true).build(),
            MenuItem.builder().name("Caesar Salad").category(MenuCategory.STARTERS).price(new BigDecimal("320")).available(true).build(),
            MenuItem.builder().name("Fresh Juice").category(MenuCategory.BEVERAGES).price(new BigDecimal("150")).available(true).build()
        ));
    }

    private void seedDiningTables() {
        if (diningTableRepository.count() > 0) return;
        diningTableRepository.saveAll(List.of(
            DiningTable.builder().tableNumber("T1").capacity(2).status(TableStatus.AVAILABLE).tableType(TableType.DINING).location("Indoor").build(),
            DiningTable.builder().tableNumber("T2").capacity(4).status(TableStatus.OCCUPIED).tableType(TableType.DINING).location("Indoor").build(),
            DiningTable.builder().tableNumber("T3").capacity(6).status(TableStatus.RESERVED).tableType(TableType.PATIO).location("Patio").build(),
            DiningTable.builder().tableNumber("B1").capacity(1).status(TableStatus.AVAILABLE).tableType(TableType.BAR).location("Bar Area").build()
        ));
    }

    private void seedInventory() {
        if (inventoryItemRepository.count() > 0) return;
        inventoryItemRepository.saveAll(List.of(
            InventoryItem.builder().name("Basmati Rice").type(InventoryType.LODGING).category("INGREDIENT").quantity(50).reorderLevel(10).unit("kg").status(InventoryStatus.IN_STOCK).build(),
            InventoryItem.builder().name("Bath Soap").type(InventoryType.LODGING).category("CONSUMABLE").quantity(3).reorderLevel(10).unit("pcs").status(InventoryStatus.LOW_STOCK).build(),
            InventoryItem.builder().name("Dishwasher Detergent").type(InventoryType.RESTAURANT).category("CONSUMABLE").quantity(0).reorderLevel(5).unit("L").status(InventoryStatus.OUT_OF_STOCK).build(),
            InventoryItem.builder().name("Table Linen").type(InventoryType.RESTAURANT).category("LINEN").quantity(20).reorderLevel(15).unit("pcs").status(InventoryStatus.IN_STOCK).build(),
            InventoryItem.builder().name("Olive Oil").type(InventoryType.RESTAURANT).category("INGREDIENT").quantity(8).reorderLevel(12).unit("L").status(InventoryStatus.LOW_STOCK).build()
        ));
    }

    private void seedActionItems() {
        if (actionItemRepository.count() > 0) return;
        actionItemRepository.saveAll(List.of(
            ActionItem.builder().title("Clean Room 101").description("Deep clean after guest checkout").category(ActionItemCategory.CLEANING).status(ActionItemStatus.TODO).build(),
            ActionItem.builder().title("Inspect Fire Extinguishers").description("Monthly safety inspection").category(ActionItemCategory.INSPECTION).status(ActionItemStatus.TODO).build(),
            ActionItem.builder().title("Restock Bar Mini-fridge").description("Restock beverages on floor 2").category(ActionItemCategory.OTHER).status(ActionItemStatus.COMPLETED).build()
        ));
    }

    private void seedBills() {
        if (billRepository.count() > 0) return;
        BigDecimal taxRate = new BigDecimal("0.08875");

        Bill bill1 = billRepository.save(Bill.builder()
                .invoiceNumber("INV-SEED-0001")
                .status(BillStatus.PAID)
                .customerName("Ravi Kumar")
                .phoneNumber("9876543210")
                .subtotal(new BigDecimal("630"))
                .taxRate(taxRate).taxAmount(new BigDecimal("55.91"))
                .discount(BigDecimal.ZERO)
                .totalDue(new BigDecimal("685.91"))
                .notes("Walk-in dinner")
                .serverName("Priya")
                .tenantSchema("default").build());
        billLineItemRepository.saveAll(List.of(
            BillLineItem.builder().bill(bill1).itemType("MENU_ITEM").description("Pasta Alfredo").quantity(1).unitPrice(new BigDecimal("450")).totalPrice(new BigDecimal("450")).tenantSchema("default").build(),
            BillLineItem.builder().bill(bill1).itemType("MENU_ITEM").description("Fresh Juice").quantity(1).unitPrice(new BigDecimal("150")).totalPrice(new BigDecimal("150")).tenantSchema("default").build()
        ));

        Bill bill2 = billRepository.save(Bill.builder()
                .invoiceNumber("INV-SEED-0002")
                .status(BillStatus.UNPAID)
                .customerName("Ananya Singh")
                .phoneNumber("9988776655")
                .subtotal(new BigDecimal("1500"))
                .taxRate(taxRate).taxAmount(new BigDecimal("133.13"))
                .discount(new BigDecimal("100"))
                .totalDue(new BigDecimal("1533.13"))
                .notes("Room 102 charges")
                .serverName("Arjun")
                .tenantSchema("default").build());
        billLineItemRepository.saveAll(List.of(
            BillLineItem.builder().bill(bill2).itemType("ROOM").description("RM 102 — STD_DOUBLE (1 NT)").quantity(1).unitPrice(new BigDecimal("1500")).totalPrice(new BigDecimal("1500")).tenantSchema("default").build()
        ));

        Bill bill3 = billRepository.save(Bill.builder()
                .invoiceNumber("INV-SEED-0003")
                .status(BillStatus.PAID)
                .customerName("Meera Patel")
                .phoneNumber("8877665544")
                .subtotal(new BigDecimal("970"))
                .taxRate(taxRate).taxAmount(new BigDecimal("86.09"))
                .discount(BigDecimal.ZERO)
                .totalDue(new BigDecimal("1056.09"))
                .serverName("Vikram")
                .tenantSchema("default").build());
        billLineItemRepository.saveAll(List.of(
            BillLineItem.builder().bill(bill3).itemType("MENU_ITEM").description("Americano").quantity(2).unitPrice(new BigDecimal("180")).totalPrice(new BigDecimal("360")).tenantSchema("default").build(),
            BillLineItem.builder().bill(bill3).itemType("MENU_ITEM").description("Caesar Salad").quantity(1).unitPrice(new BigDecimal("320")).totalPrice(new BigDecimal("320")).tenantSchema("default").build(),
            BillLineItem.builder().bill(bill3).itemType("CUSTOM").description("Mineral Water").quantity(2).unitPrice(new BigDecimal("50")).totalPrice(new BigDecimal("100")).tenantSchema("default").build(),
            BillLineItem.builder().bill(bill3).itemType("CUSTOM").description("Candle Set").quantity(1).unitPrice(new BigDecimal("190")).totalPrice(new BigDecimal("190")).tenantSchema("default").build()
        ));
    }
}
