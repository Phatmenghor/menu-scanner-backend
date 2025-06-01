package com.menghor.ksit.enumations;

public enum RequestStatus {
    PENDING,    // Initial state when student submits request
    ACCEPTED,   // Staff accepts the request
    DONE,       // Request is completed
    REJECTED,    // Staff rejects the request
    RETURN,   // Request is deleted by the student
    DELETED,   // Request is deleted by the staff
}