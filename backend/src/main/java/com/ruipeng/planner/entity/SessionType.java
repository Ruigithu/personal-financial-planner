package com.ruipeng.planner.entity;

public enum SessionType {
    INITIAL_CONSULTATION("Initial Consultation"),
    STANDARD_SESSION("Standard Session"),
    FOLLOWUP_SESSION("Follow-up Session"),
    PLAN_REVIEW("Financial Plan Review");

    private final String displayName;

    SessionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
