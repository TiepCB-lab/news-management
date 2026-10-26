package com.uth.newsmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uth.newsmanagement.entity.Category;
import com.uth.newsmanagement.entity.News;
import com.uth.newsmanagement.repository.CategoryRepository;
import com.uth.newsmanagement.repository.NewsRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** H2 riêng cho test; không kết nối database phát triển và không ghi vào uploads thật. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:news_tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.flyway.enabled=false"
})
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NewsApiIntegrationTest {

    private static final String PUBLIC = "/api/v1/public/news";
    private static final String PRIVATE = "/api/v1/admin/news";

    @TempDir
    static Path uploadDir;

    @DynamicPropertySource
    static void testProperties(DynamicPropertyRegistry registry) {
        registry.add("app.upload.dir", () -> uploadDir.toString());
        registry.add("app.jwt.secret", () -> "test-only-secret-key-at-least-256-bits-for-news-api");
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired NewsRepository newsRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired EntityManager entityManager;

    private Category technology;
    private Category education;

    @BeforeEach
    void prepareNews() {
        newsRepository.deleteAll();
        technology = categoryRepository.save(new Category("Test Technology", "test-tech", ""));
        education = categoryRepository.save(new Category("Test Education", "test-edu", ""));
    }

    @Test
    void publicSearchCombinesKeywordCategoryAndPublishedStatus() throws Exception {
        News expected = article("Java basics", "", technology, true);
        article("Other title", "Java summary", education, true);
        article("Java draft", "", technology, false);

        mvc.perform(get(PUBLIC).param("keyword", "  JAVA  ")
                        .param("categoryId", technology.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.totalElements").value(1))
                .andExpect(jsonPath("$.data[0].id").value(expected.getId()));
    }

    @Test
    void privateSearchIncludesDraftsAndMatchesSummary() throws Exception {
        News expected = article("Different title", "Learn Java", technology, false);
        article("Java education", "", education, true);

        mvc.perform(get(PRIVATE).header("Authorization", login())
                        .param("keyword", "java").param("categoryId", technology.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(expected.getId()));
    }

    @Test
    void defaultsAndBlankKeywordRespectCategory() throws Exception {
        article("One", "", technology, true);
        article("Two", "", education, true);
        article("Draft", "", technology, false);

        mvc.perform(get(PUBLIC)).andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.page").value(1))
                .andExpect(jsonPath("$.meta.limit").value(10))
                .andExpect(jsonPath("$.meta.totalElements").value(2));
        mvc.perform(get(PUBLIC).param("keyword", "   ")
                        .param("categoryId", technology.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.totalElements").value(1));
    }

    @Test
    void paginationUsesStableSecondaryIdSortAndAccurateMetadata() throws Exception {
        article("Alpha", "", technology, true);
        News second = article("Alpha", "", technology, true);
        article("Zulu", "", technology, true);

        mvc.perform(get(PUBLIC).param("page", "2").param("limit", "1")
                        .param("sortBy", "title").param("sortDir", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(second.getId()))
                .andExpect(jsonPath("$.meta.page").value(2))
                .andExpect(jsonPath("$.meta.totalElements").value(3))
                .andExpect(jsonPath("$.meta.totalPages").value(3));
        mvc.perform(get(PUBLIC).param("page", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.meta.totalElements").value(3));
    }

    @ParameterizedTest
    @ValueSource(strings = {"%", "_", "\\"})
    void searchTreatsSqlWildcardsAsLiteralCharacters(String keyword) throws Exception {
        News expected = article("Contains " + keyword, "", technology, true);
        article("Contains X", "", technology, true);
        mvc.perform(get(PUBLIC).param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(expected.getId()));
    }

    static Stream<Arguments> invalidQueries() {
        return Stream.of(PUBLIC, PRIVATE).flatMap(endpoint -> Stream.of(
                new String[]{"page", "0"}, new String[]{"page", "-1"}, new String[]{"page", "abc"},
                new String[]{"limit", "0"}, new String[]{"limit", "-1"}, new String[]{"limit", "101"},
                new String[]{"limit", "abc"}, new String[]{"sortBy", "content"},
                new String[]{"sortBy", "author.username"}, new String[]{"sortDir", "sideways"},
                new String[]{"categoryId", "0"}, new String[]{"categoryId", "-1"},
                new String[]{"categoryId", "abc"}, new String[]{"keyword", "x".repeat(201)}
        ).map(pair -> Arguments.of(endpoint, pair[0], pair[1])));
    }

    @ParameterizedTest
    @MethodSource("invalidQueries")
    void rejectsInvalidQueryWithFieldError(String endpoint, String key, String value) throws Exception {
        mvc.perform(get(endpoint).param(key, value)
                        .header("Authorization", endpoint.equals(PRIVATE) ? login() : ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data." + key).exists());
    }

    @Test
    void privateListRequiresAuthentication() throws Exception {
        mvc.perform(get(PRIVATE)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401));
    }

    @Test
    void malformedJsonAndInvalidPathIdReturnBadRequest() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
        mvc.perform(put(PRIVATE + "/invalid").header("Authorization", login())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void draftDetailCannotBeReadPubliclyByIdOrSlug() throws Exception {
        News draft = article("Draft", "", technology, false);
        for (String key : List.of(draft.getId().toString(), draft.getSlug())) {
            mvc.perform(get(PUBLIC + "/" + key)).andExpect(status().isNotFound());
        }
    }

    @Test
    void readingDetailIncrementsCountWithoutUpdatingArticleTimestamp() throws Exception {
        News news = article("Published", "", technology, true);
        entityManager.flush();
        Long id = news.getId();
        // Lấy timestamp đã được DB lưu để so sánh cùng độ chính xác.
        entityManager.clear();
        var updatedAt = newsRepository.findById(id).orElseThrow().getUpdatedAt();
        for (int count = 1; count <= 2; count++) {
            entityManager.clear();
            mvc.perform(get(PUBLIC + "/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.viewCount").value(count));
            entityManager.flush();
        }
        entityManager.clear();
        News stored = newsRepository.findById(id).orElseThrow();
        assertThat(stored.getViewCount()).isEqualTo(2);
        assertThat(stored.getUpdatedAt()).isEqualTo(updatedAt);
    }

    private News article(String title, String summary, Category category, boolean published) {
        News news = new News();
        news.setTitle(title);
        news.setSummary(summary);
        news.setContent("Test content");
        news.setSlug(UUID.randomUUID().toString());
        news.setCategory(category);
        news.setIsPublished(published);
        return newsRepository.save(news);
    }

    private String login() throws Exception {
        String response = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(response).path("data").path("accessToken").asText();
    }
}
