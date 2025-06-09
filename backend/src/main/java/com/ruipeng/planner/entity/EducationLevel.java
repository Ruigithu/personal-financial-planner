package com.ruipeng.planner.entity;

public enum EducationLevel {
    HIGH_SCHOOL("High School"),
    ASSOCIATE("Associate Degree"),
    BACHELOR("Bachelor's Degree"),
    MASTER("Master's Degree"),
    DOCTORATE("Doctorate or Higher");

    private final String displayName;

    EducationLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
