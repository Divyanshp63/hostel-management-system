package com.hostel.management.enums;

import lombok.Getter;

@Getter
public enum ComplaintPriority {
    LOW("Low", 48, "bg-info-subtle text-info border border-info-subtle"),
    MEDIUM("Medium", 24, "bg-warning-subtle text-warning-emphasis border border-warning-subtle"),
    HIGH("High", 8, "bg-danger-subtle text-danger border border-danger-subtle"),
    CRITICAL("Critical", 4, "bg-danger text-white");

    private final String displayName;
    private final int slaHours;
    private final String badgeClass;

    ComplaintPriority(String displayName, int slaHours, String badgeClass) {
        this.displayName = displayName;
        this.slaHours = slaHours;
        this.badgeClass = badgeClass;
    }
}
