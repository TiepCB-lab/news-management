package com.uth.newsmanagement.repository;

import com.uth.newsmanagement.entity.News;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NewsSpecifications {

    private NewsSpecifications() { }

    public static Specification<News> matching(String keyword, Long categoryId, boolean publishedOnly) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (publishedOnly) {
                predicates.add(cb.isTrue(root.get("isPublished")));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (StringUtils.hasText(keyword)) {
                // Tìm chuỗi người dùng nhập, không coi % hoặc _ là wildcard SQL.
                String escaped = keyword.trim().toLowerCase(Locale.ROOT)
                        .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                String pattern = "%" + escaped + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern, '\\'),
                        cb.like(cb.lower(root.get("summary")), pattern, '\\')));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
