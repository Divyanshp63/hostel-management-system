package com.hostel.management.enums;

import lombok.Getter;

@Getter
public enum ComplaintCategory {
    ELECTRICAL("Electrical", "bi-lightning-charge-fill"),
    PLUMBING("Plumbing", "bi-droplet-fill"),
    HOUSEKEEPING("Housekeeping", "bi-stars"),
    INTERNET_IT("Internet / IT", "bi-wifi"),
    MAINTENANCE("Maintenance", "bi-tools"),
    MESS("Mess", "bi-egg-fried"),
    OTHER("Other", "bi-question-circle-fill");

    private final String displayName;
    private final String iconClass;

    ComplaintCategory(String displayName, String iconClass) {
        this.displayName = displayName;
        this.iconClass = iconClass;
    }
}
