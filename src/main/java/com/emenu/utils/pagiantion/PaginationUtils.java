package com.emenu.utils.pagiantion;

import com.emenu.exceptoins.error.InvalidPaginationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility class for pagination-related operations
 */
@Slf4j
public class PaginationUtils {

    /**
     * Maximum allowed page size to prevent performance issues
     */
    private static final int MAX_PAGE_SIZE = 1000;

    /**
     * Validates pagination parameters.
     *
     * @param pageNo   the page number to validate
     * @param pageSize the page size to validate
     * @throws InvalidPaginationException if validation fails
     */
    public static void validatePagination(Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo <= 0) {
            throw new InvalidPaginationException("Page number must be greater than 0");
        }
        if (pageSize == null || pageSize <= 0) {
            throw new InvalidPaginationException("Page size must be greater than 0");
        }

        if (pageSize > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException("Page size cannot exceed " + MAX_PAGE_SIZE);
        }
    }

    /**
     * Creates a pageable object with default sorting by creation date (descending)
     *
     * @param pageNo Page number (1-based)
     * @param pageSize Number of items per page
     * @return Configured Pageable object
     */
    public static Pageable createPageable(Integer pageNo, Integer pageSize) {
        // Apply default values if null
        pageNo = (pageNo == null) ? 1 : pageNo;
        pageSize = (pageSize == null) ? 10 : pageSize;

        // Validate pagination parameters
        validatePagination(pageNo, pageSize);

        // Convert to 0-based page number
        int zeroBasedPageNo = pageNo - 1;

        log.debug("Creating pageable with page {} (zero-based: {}), size {}, sorted by createdAt DESC",
                pageNo, zeroBasedPageNo, pageSize);

        // Create pageable with sorting by createdAt in descending order
        return PageRequest.of(zeroBasedPageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /**
     * Creates a pageable object with custom sorting
     *
     * @param pageNo Page number (1-based)
     * @param pageSize Number of items per page
     * @param sortBy Field to sort by
     * @param sortDirection Sort direction (ASC or DESC)
     * @return Configured Pageable object
     */
    public static Pageable createPageable(Integer pageNo, Integer pageSize, String sortBy, String sortDirection) {
        // Apply default values if null
        pageNo = (pageNo == null) ? 1 : pageNo;
        pageSize = (pageSize == null) ? 10 : pageSize;
        sortBy = (sortBy == null || sortBy.isEmpty()) ? "createdAt" : sortBy;

        // Validate pagination parameters
        validatePagination(pageNo, pageSize);

        // Convert to 0-based page number
        int zeroBasedPageNo = pageNo - 1;

        // Determine sort direction
        Sort.Direction direction = Sort.Direction.DESC; // Default
        if (sortDirection != null && sortDirection.equalsIgnoreCase("ASC")) {
            direction = Sort.Direction.ASC;
        }

        log.debug("Creating pageable with page {} (zero-based: {}), size {}, sorted by {} {}",
                pageNo, zeroBasedPageNo, pageSize, sortBy, direction);

        return PageRequest.of(zeroBasedPageNo, pageSize, Sort.by(direction, sortBy));
    }
}