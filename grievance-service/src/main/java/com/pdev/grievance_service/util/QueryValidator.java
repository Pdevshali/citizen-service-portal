package com.pdev.grievance_service.util;

import java.util.Map;

public class QueryValidator {

    public static int normalizePage(int page) {
        return Math.max(page, 0);
    }

    public static int normalizeSize(int size) {
        return Math.clamp(size, 1, 100);
    }

    public static String normalizeSortBy(
            String sortBy,
            Map<String, String> allowedFields) {

        return allowedFields.containsKey(sortBy)
                ? sortBy
                : "createdAt";
    }

    public static String normalizeSortDir(String sortDir) {

        return "asc".equalsIgnoreCase(sortDir) ? "asc" : "desc";
    }
}