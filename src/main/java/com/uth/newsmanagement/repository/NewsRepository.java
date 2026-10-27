package com.uth.newsmanagement.repository;

import com.uth.newsmanagement.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {

    Optional<News> findBySlug(String slug);

    Optional<News> findBySlugAndIsPublishedTrue(String slug);

    Optional<News> findByIdAndIsPublishedTrue(Long id);

    @Modifying
    @Query("UPDATE News n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
