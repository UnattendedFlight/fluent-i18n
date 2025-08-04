package com.example.fluenti18n.service;

import com.example.fluenti18n.model.Order;
import com.example.fluenti18n.model.OrderStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderService {
    
    public List<Order> getAllOrders() {
        return Arrays.asList(
            new Order("#12345", "John Doe", "Premium Widget", new BigDecimal("299.99"), OrderStatus.PROCESSING, LocalDate.of(2024, 8, 1)),
            new Order("#12346", "Jane Smith", "Basic Widget", new BigDecimal("99.99"), OrderStatus.PENDING, LocalDate.of(2024, 8, 2)),
            new Order("#12347", "Bob Johnson", "Deluxe Widget", new BigDecimal("499.99"), OrderStatus.SHIPPED, LocalDate.of(2024, 7, 30)),
            new Order("#12348", "Alice Brown", "Standard Widget", new BigDecimal("199.99"), OrderStatus.DELIVERED, LocalDate.of(2024, 7, 28)),
            new Order("#12349", "Charlie Wilson", "Pro Widget", new BigDecimal("399.99"), OrderStatus.PROCESSING, LocalDate.of(2024, 8, 3))
        );
    }
    
    public int getTotalOrders() {
        return getAllOrders().size();
    }
    
    public int getPendingOrders() {
        return (int) getAllOrders().stream()
            .filter(order -> OrderStatus.PENDING.equals(order.getStatus()))
            .count();
    }
    
    public int getProcessingOrders() {
        return (int) getAllOrders().stream()
            .filter(order -> OrderStatus.PROCESSING.equals(order.getStatus()))
            .count();
    }
    
    public int getCompletedOrders() {
        return (int) getAllOrders().stream()
            .filter(order -> OrderStatus.DELIVERED.equals(order.getStatus()))
            .count();
    }
} 