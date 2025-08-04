package com.example.fluenti18n.model;

import io.github.unattendedflight.fluent.i18n.I18n;

/**
 * Order status enum with translatable natural language descriptions
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;
    
    /**
     * Get the translatable natural language description
     */
    public String getRepresentation() {
        return switch (this) {
            case PENDING -> I18n.t("Pending");
            case PROCESSING -> I18n.t("Processing");
            case SHIPPED -> I18n.t("Shipped");
            case DELIVERED -> I18n.t("Delivered");
            case CANCELLED -> I18n.t("Cancelled");
        };
    }
} 