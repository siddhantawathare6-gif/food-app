package com.food.order.entity;

public enum OrderStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    PREPARING("Preparing"),
    READY("Ready for pickup"),
    OUT_FOR_DELIVERY("Out for delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCancellable() {
        return this != DELIVERED && this != CANCELLED;
    }

    public boolean isEditable() {
        return this == PENDING || this == CONFIRMED;
    }
}
