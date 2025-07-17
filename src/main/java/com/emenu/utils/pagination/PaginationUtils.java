package com.emenu.utils.pagination;

import com.emenu.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Slf4j
public class PaginationUtils {
    
    private static final int MAX_PAGE_SIZE = 1000;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_PAGE_NUMBER = 0;

    public static void validatePagination(Integer pageNo, Integer pageSize) {
        if (pageNo != null && pageNo < 0) {
            throw new ValidationException("Page number must be greater than or equal to 0");
        }
        if (pageSize != null && pageSize <= 0) {
            throw new ValidationException("Page size must be greater than 0");
        }
        if (pageSize != null && pageSize > MAX_PAGE_SIZE) {
            throw new ValidationException("Page size cannot exceed " + MAX_PAGE_SIZE);
        }
    }

    public static Pageable createPageable(Integer pageNo, Integer pageSize) {
        pageNo = (pageNo == null) ? DEFAULT_PAGE_NUMBER : pageNo;
        pageSize = (pageSize == null) ? DEFAULT_PAGE_SIZE : pageSize;
        
        validatePagination(pageNo, pageSize);
        
        return PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public static Pageable createPageable(Integer pageNo, Integer pageSize, String sortBy, String sortDirection) {
        pageNo = (pageNo == null) ? DEFAULT_PAGE_NUMBER : pageNo;
        pageSize = (pageSize == null) ? DEFAULT_PAGE_SIZE : pageSize;
        sortBy = (sortBy == null || sortBy.isEmpty()) ? "createdAt" : sortBy;
        
        validatePagination(pageNo, pageSize);
        
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("ASC")) {
            direction = Sort.Direction.ASC;
        }
        
        return PageRequest.of(pageNo, pageSize, Sort.by(direction, sortBy));
    }
}
