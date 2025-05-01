package com.menghor.ksit.utils.pagiantion;

import com.menghor.ksit.exceptoins.error.InvalidPaginationException;

public class PaginationUtils {

    /**
     * Validates pagination parameters.
     *
     * @param pageNo   the page number to validate
     * @param pageSize the page size to validate
     * @return an ApiResponse indicating success or error
     */
    public static void validatePagination(int pageNo, int pageSize) {
        if (pageNo <= 0) {
            throw new InvalidPaginationException("Page number must be greater than 0");
        }
        if (pageSize <= 0) {
            throw new InvalidPaginationException("Page size must be greater than 0");
        }
    }
}
