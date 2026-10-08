package com.hostel.management.enums;

import lombok.Getter;

@Getter
public enum ComplaintStatus {
    SUBMITTED("Submitted", "badge-submitted bg-secondary-subtle text-secondary"),
    IN_PROGRESS("In Progress", "badge-in-progress bg-warning-subtle text-warning-emphasis"),
    RESOLVED_BY_MAINTENANCE("Resolved by Maintenance", "badge-resolved bg-info-subtle text-info"),
    RETURNED("Returned", "badge-returned bg-danger-subtle text-danger"),
    VERIFIED("Verified", "badge-verified bg-teal-subtle text-teal"),
    CLOSED("Closed", "badge-closed bg-dark-subtle text-dark"),
    REJECTED("Rejected", "badge-rejected bg-danger-subtle text-danger");

    private final String displayName;
    private final String badgeClass;

    ComplaintStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }
}
