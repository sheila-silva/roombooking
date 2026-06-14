package com.roombooking.domain.enums;

public enum BookingStatus {
    ACTIVE,
    CANCELLED;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
