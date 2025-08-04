package com.example.fluenti18n.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Order {
    private String id;
    private String customerName;
    private String productName;
    private BigDecimal amount;
    private OrderStatus status;
    private LocalDate orderDate;
    
    public Order(String id, String customerName, String productName, BigDecimal amount, OrderStatus status, LocalDate orderDate) {
        this.id = id;
        this.customerName = customerName;
        this.productName = productName;
        this.amount = amount;
        this.status = status;
        this.orderDate = orderDate;
    }
    
    // Getters
    public String getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getProductName() { return productName; }
    public BigDecimal getAmount() { return amount; }
    public OrderStatus getStatus() { return status; }
    public LocalDate getOrderDate() { return orderDate; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
} 