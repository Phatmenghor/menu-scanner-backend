
package com.emenu.enums.hr;

import lombok.Getter;

@Getter
public enum LeaveStatusEnum {
    PENDING("Pending", "Waiting for approval"),
    APPROVED("Approved", "Leave approved by manager"),
    REJECTED("Rejected", "Leave request rejected"),
    CANCELLED("Cancelled", "Leave cancelled by employee");

    private final String displayName;
    private final String description;

    LeaveStatusEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isApproved() {
        return this == APPROVED;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }
}


