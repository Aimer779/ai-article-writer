package cn.nuist.aiarticlewriter.common;

import lombok.Data;

/**
 * Pagination request wrapper
 */
@Data
public class PageRequest {

    /**
     * Page number
     */
    private int pageNum = 1;

    /**
     * Page size
     */
    private int pageSize = 10;

    /**
     * Sort field
     */
    private String sortField;

    /**
     * Sort order (default descending)
     */
    private String sortOrder = "descend";
}