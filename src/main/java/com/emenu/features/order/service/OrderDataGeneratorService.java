package com.emenu.features.order.service;

import com.emenu.enums.common.Status;
import com.emenu.enums.payment.PaymentMethod;
import com.emenu.features.order.models.Order;
import com.emenu.features.order.models.OrderItem;
import com.emenu.features.order.models.OrderProcessStatus;
import com.emenu.features.order.repository.OrderProcessStatusRepository;
import com.emenu.features.order.repository.OrderRepository;
import com.emenu.shared.generate.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDataGeneratorService {

    private final OrderProcessStatusRepository orderProcessStatusRepository;
    private final OrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;

    private static final String[] PRODUCT_NAMES = {
            "Fried Rice", "Pad Thai", "Tom Yum Soup", "Green Curry", "Spring Rolls",
            "Beef Noodles", "Chicken Satay", "Mango Sticky Rice", "Papaya Salad", "BBQ Pork"
    };

    private static final String[] PROVINCES = {
            "Phnom Penh", "Siem Reap", "Battambang", "Kampong Cham", "Kandal"
    };

    private static final PaymentMethod[] PAYMENT_METHODS = PaymentMethod.values();

    @Transactional
    public void generateOrderStatuses(UUID businessId) {
        log.info("Generating order statuses for business: {}", businessId);

        List<OrderProcessStatus> statuses = Arrays.asList(
                createStatus(businessId, "Pending", "Order received, waiting for confirmation"),
                createStatus(businessId, "Confirmed", "Order confirmed by restaurant"),
                createStatus(businessId, "Preparing", "Food is being prepared"),
                createStatus(businessId, "Ready", "Order is ready for pickup/delivery"),
                createStatus(businessId, "Out for Delivery", "Order is on the way"),
                createStatus(businessId, "Delivered", "Order successfully delivered"),
                createStatus(businessId, "Completed", "Order completed"),
                createStatus(businessId, "Cancelled", "Order cancelled")
        );

        orderProcessStatusRepository.saveAll(statuses);
        log.info("Created {} order statuses", statuses.size());
    }

    @Transactional
    public void generateOrders(UUID businessId, int count, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating {} orders for business: {} from {} to {}", count, businessId, startDate, endDate);

        // Get order statuses
        List<OrderProcessStatus> statuses = orderProcessStatusRepository.findByBusinessIdOrderByCreatedAtAsc(businessId);
        if (statuses.isEmpty()) {
            throw new RuntimeException("No order statuses found. Please generate statuses first.");
        }

        int batchSize = 1000;
        int totalBatches = (count + batchSize - 1) / batchSize;

        for (int batch = 0; batch < totalBatches; batch++) {
            int batchStart = batch * batchSize;
            int batchEnd = Math.min(batchStart + batchSize, count);
            List<Order> orders = new ArrayList<>();

            for (int i = batchStart; i < batchEnd; i++) {
                orders.add(generateRandomOrder(businessId, statuses, startDate, endDate));
            }

            orderRepository.saveAll(orders);
            log.info("Saved batch {}/{} ({} orders)", batch + 1, totalBatches, orders.size());
        }

        log.info("Successfully generated {} orders", count);
    }

    private OrderProcessStatus createStatus(UUID businessId, String name, String description) {
        OrderProcessStatus status = new OrderProcessStatus();
        status.setBusinessId(businessId);
        status.setName(name);
        status.setDescription(description);
        status.setStatus(Status.ACTIVE);
        return status;
    }

    private Order generateRandomOrder(UUID businessId, List<OrderProcessStatus> statuses,
                                      LocalDateTime startDate, LocalDateTime endDate) {
        Order order = new Order();

        // Basic info
        order.setOrderNumber(orderNumberGenerator.generateUniqueOrderNumber(orderRepository::existsByOrderNumber));
        order.setBusinessId(businessId);
        order.setCustomerId(UUID.randomUUID()); // Random customer

        // Random date within range
        LocalDateTime orderDate = randomDateBetween(startDate, endDate);
        order.setCreatedAt(orderDate);
        order.setUpdatedAt(orderDate);

        // Random status
        OrderProcessStatus status = statuses.get(ThreadLocalRandom.current().nextInt(statuses.size()));
        order.setOrderProcessStatusId(status.getId());

        // Status-based timestamps
        if (Arrays.asList("Confirmed", "Preparing", "Ready", "Delivered", "Completed").contains(status.getName())) {
            order.setConfirmedAt(orderDate.plusMinutes(ThreadLocalRandom.current().nextInt(5, 30)));
        }
        if (Arrays.asList("Delivered", "Completed").contains(status.getName())) {
            order.setCompletedAt(orderDate.plusMinutes(ThreadLocalRandom.current().nextInt(30, 120)));
        }

        // Delivery info (70% of orders have delivery)
        if (ThreadLocalRandom.current().nextDouble() < 0.7) {
            String province = PROVINCES[ThreadLocalRandom.current().nextInt(PROVINCES.length)];
            order.setDeliveryAddressSnapshot(generateRandomAddress(province));
            order.setDeliveryOptionName("Standard Delivery");
            order.setDeliveryOptionDescription("30-45 minutes");
            order.setDeliveryFee(new BigDecimal(ThreadLocalRandom.current().nextDouble(1.0, 5.0))
                    .setScale(2, BigDecimal.ROUND_HALF_UP));
        } else {
            order.setDeliveryFee(BigDecimal.ZERO);
        }

        // Payment
        order.setPaymentMethod(PAYMENT_METHODS[ThreadLocalRandom.current().nextInt(PAYMENT_METHODS.length)]);
        order.setIsPaid(ThreadLocalRandom.current().nextBoolean());

        // Order items
        int itemCount = ThreadLocalRandom.current().nextInt(1, 6);
        List<OrderItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (int i = 0; i < itemCount; i++) {
            OrderItem item = generateRandomOrderItem(order.getId());
            items.add(item);
            subtotal = subtotal.add(item.getTotalPrice());
        }

        order.setItems(items);
        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal.add(order.getDeliveryFee()));

        // Notes (30% have notes)
        if (ThreadLocalRandom.current().nextDouble() < 0.3) {
            order.setCustomerNote("Please call when arriving");
        }

        return order;
    }

    private OrderItem generateRandomOrderItem(UUID orderId) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(UUID.randomUUID());
        item.setProductName(PRODUCT_NAMES[ThreadLocalRandom.current().nextInt(PRODUCT_NAMES.length)]);
        item.setSizeName(ThreadLocalRandom.current().nextBoolean() ? "Regular" : "Large");

        // Pricing
        BigDecimal basePrice = new BigDecimal(ThreadLocalRandom.current().nextDouble(3.0, 15.0))
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        boolean hasDiscount = ThreadLocalRandom.current().nextDouble() < 0.3; // 30% have discounts
        BigDecimal finalPrice = basePrice;

        if (hasDiscount) {
            BigDecimal discount = basePrice.multiply(new BigDecimal("0.20")); // 20% off
            finalPrice = basePrice.subtract(discount);
            item.setCurrentPrice(basePrice);
            item.setFinalPrice(finalPrice);
            item.setHasPromotion(true);
            item.setPromotionType("PERCENTAGE");
            item.setPromotionValue(new BigDecimal("20"));
        } else {
            item.setCurrentPrice(basePrice);
            item.setFinalPrice(finalPrice);
            item.setHasPromotion(false);
        }

        item.setUnitPrice(finalPrice);
        item.setQuantity(ThreadLocalRandom.current().nextInt(1, 4));
        item.setTotalPrice(finalPrice.multiply(BigDecimal.valueOf(item.getQuantity())));

        return item;
    }

    private String generateRandomAddress(String province) {
        String[] villages = {"Village A", "Village B", "Village C"};
        String village = villages[ThreadLocalRandom.current().nextInt(villages.length)];
        return String.format("%s, %s District, %s", village, province, province);
    }

    private LocalDateTime randomDateBetween(LocalDateTime start, LocalDateTime end) {
        long startEpoch = start.toEpochSecond(java.time.ZoneOffset.UTC);
        long endEpoch = end.toEpochSecond(java.time.ZoneOffset.UTC);
        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        return LocalDateTime.ofEpochSecond(randomEpoch, 0, java.time.ZoneOffset.UTC);
    }
}
