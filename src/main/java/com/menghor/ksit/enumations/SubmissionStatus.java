package com.menghor.ksit.enumations;

public enum SubmissionStatus {
    DRAFT,        // Initial state, teacher can edit
    SUBMITTED,    // Submitted for review
    PENDING,      // Under review
    APPROVED,     // Final state, approved by staff
    REJECTED      // Needs revision, sent back to teacher
}