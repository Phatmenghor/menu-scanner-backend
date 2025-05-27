package com.menghor.ksit.enumations;

import lombok.Getter;

@Getter
public enum GradeLevel {
    A_PLUS("A+", 95.0, 100.0),
    A("A", 90.0, 94.99),
    A_MINUS("A-", 85.0, 89.99),
    B_PLUS("B+", 80.0, 84.99),
    B("B", 75.0, 79.99),
    B_MINUS("B-", 70.0, 74.99),
    C_PLUS("C+", 65.0, 69.99),
    C("C", 60.0, 64.99),
    C_MINUS("C-", 55.0, 59.99),
    D_PLUS("D+", 50.0, 54.99),
    D("D", 45.0, 49.99),
    D_MINUS("D-", 40.0, 44.99),
    F("F", 0.0, 39.99);

    private final String grade;
    private final double minScore;
    private final double maxScore;

    GradeLevel(String grade, double minScore, double maxScore) {
        this.grade = grade;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public static GradeLevel fromScore(double score) {
        for (GradeLevel gradeLevel : GradeLevel.values()) {
            if (score >= gradeLevel.getMinScore() && score <= gradeLevel.getMaxScore()) {
                return gradeLevel;
            }
        }
        return F; // Default to F if no match found
    }
}