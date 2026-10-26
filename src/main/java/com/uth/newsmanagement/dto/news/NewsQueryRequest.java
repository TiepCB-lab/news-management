package com.uth.newsmanagement.dto.news;

import com.uth.newsmanagement.common.constant.AppConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Query parameters dùng chung cho danh sách tin public và private. */
public class NewsQueryRequest {

    @Min(value = 1, message = "page phải lớn hơn hoặc bằng 1")
    private int page = Integer.parseInt(AppConstants.DEFAULT_PAGE_NUMBER);

    @Min(value = 1, message = "limit phải lớn hơn hoặc bằng 1")
    @Max(value = AppConstants.MAX_PAGE_SIZE, message = "limit không được vượt quá 100")
    private int limit = Integer.parseInt(AppConstants.DEFAULT_PAGE_SIZE);

    @Pattern(regexp = "id|title|createdAt|updatedAt|viewCount",
            message = "sortBy chỉ nhận id, title, createdAt, updatedAt hoặc viewCount")
    private String sortBy = AppConstants.DEFAULT_SORT_BY;

    @Pattern(regexp = "(?i)asc|desc", message = "sortDir chỉ nhận asc hoặc desc")
    private String sortDir = AppConstants.DEFAULT_SORT_DIRECTION;

    @Size(max = 200, message = "keyword không được vượt quá 200 ký tự")
    private String keyword;

    @Positive(message = "categoryId phải là số nguyên dương")
    private Long categoryId;

    public Pageable toPageable() {
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Sort sort = Sort.by(direction, sortBy);
        // ID làm tiêu chí phụ để thứ tự ổn định khi nhiều bài có cùng giá trị sortBy.
        if (!"id".equals(sortBy)) {
            sort = sort.and(Sort.by(direction, "id"));
        }
        return PageRequest.of(page - 1, limit, sort);
    }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
