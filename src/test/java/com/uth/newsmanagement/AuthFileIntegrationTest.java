package com.uth.newsmanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uth.newsmanagement.entity.News;
import com.uth.newsmanagement.entity.Role;
import com.uth.newsmanagement.repository.NewsRepository;
import com.uth.newsmanagement.repository.UserRepository;
import com.uth.newsmanagement.service.FileStorageService;
import com.uth.newsmanagement.common.exception.BadRequestException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:auth_file_tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false", "spring.flyway.enabled=false",
        "spring.servlet.multipart.max-file-size=1KB", "spring.servlet.multipart.max-request-size=2KB",
        "app.cors.allowed-origins=http://localhost:5173"
})
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthFileIntegrationTest {
    private static final String SECRET = "auth-file-test-secret-key-at-least-256-bits-long";
    private static final String UPLOAD = "/api/v1/files/upload";
    private static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+j5l8AAAAASUVORK5CYII=");

    @TempDir static Path uploadDir;
    @DynamicPropertySource
    static void testProperties(DynamicPropertyRegistry registry) {
        registry.add("app.upload.dir", () -> uploadDir.toString());
        registry.add("app.jwt.secret", () -> SECRET);
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired NewsRepository news;
    @Autowired PasswordEncoder encoder;
    @Autowired FileStorageService storage;
    @LocalServerPort int port;

    @Test
    void registerNormalizesBeforeValidationAndAlwaysCreatesUserRole() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "  Student_1  ",
                                "email", "  STUDENT@EXAMPLE.COM  ", "password", "secret123", "role", "ROLE_ADMIN"))))
                .andExpect(status().isCreated());
        var saved = users.findByUsername("Student_1").orElseThrow();
        assertThat(saved.getEmail()).isEqualTo("student@example.com");
        assertThat(saved.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(saved.getPassword()).isNotEqualTo("secret123");
        assertThat(encoder.matches("secret123", saved.getPassword())).isTrue();
        assertThat(login(" STUDENT@EXAMPLE.COM ", "secret123")).startsWith("Bearer ");
        assertThat(login(" Student_1 ", "secret123")).startsWith("Bearer ");
    }

    @ParameterizedTest
    @CsvSource({"' admin ',different@example.com", "different,' ADMIN@UTH.EDU.VN '"})
    void duplicateCanonicalUsernameOrEmailReturnsConflict(String username, String email) throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "email", email, "password", "secret123"))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.statusCode").value(409));
    }

    @ParameterizedTest
    @CsvSource({"'  ab  ',student@example.com", "'  ',student@example.com", "admin@uth.edu.vn,student@example.com",
            "validname,invalid-email"})
    void invalidRegistrationReturnsFieldErrors(String username, String email) throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "email", email, "password", "secret123"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.data").isMap());
    }

    @ParameterizedTest
    @ValueSource(strings = {"x", "€"})
    void passwordsOverBcryptByteLimitDoNotCauseServerError(String character) throws Exception {
        String password = character.repeat(73 / character.getBytes(StandardCharsets.UTF_8).length + 1);
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "newuser", "email", "new@example.com", "password", password))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "admin", "password", password))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsPasswordAtBcryptLimit() throws Exception {
        String password = "x".repeat(72);
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "newuser", "email", "new@example.com", "password", password))))
                .andExpect(status().isCreated());
        login("newuser", password);
    }

    @ParameterizedTest
    @CsvSource({"admin,wrong-password", "unknown,secret123"})
    void badCredentialsReturnUnauthorized(String username, String password) throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.statusCode").value(401));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void simultaneousDuplicateRegistrationHasOneWinner() throws Exception {
        String username = "race-" + UUID.randomUUID();
        String body = json.writeValueAsString(Map.of("username", username, "email", username + "@example.com", "password", "secret123"));
        var executor = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            var tasks = java.util.stream.IntStream.range(0, 2).mapToObj(i -> executor.submit(() -> {
                start.await();
                return mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                        .andReturn().getResponse().getStatus();
            })).toList();
            start.countDown();
            assertThat(List.of(tasks.get(0).get(10, TimeUnit.SECONDS), tasks.get(1).get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
        } finally {
            executor.shutdownNow();
            users.findByUsername(username).ifPresent(user -> users.deleteById(user.getId()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"malformed", "expired", "wrong-signature"})
    void invalidJwtCannotReadPrivateNews(String kind) throws Exception {
        String token = "not-a-jwt";
        if (!kind.equals("malformed")) {
            String key = kind.equals("wrong-signature") ? SECRET + "wrong" : SECRET;
            token = Jwts.builder().subject("admin")
                    .expiration(Date.from(Instant.now().plusSeconds(kind.equals("expired") ? -60 : 60)))
                    .signWith(Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8))).compact();
        }
        mvc.perform(get("/api/v1/admin/news").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.statusCode").value(401));
    }

    @Test
    void userCanReadPrivateNewsButOnlyAdminCanDelete() throws Exception {
        News article = new News();
        article.setTitle("Role check");
        article.setContent("Content");
        article.setSlug(UUID.randomUUID().toString());
        article = news.saveAndFlush(article);
        String user = login("author", "author123");
        mvc.perform(get("/api/v1/admin/news").header("Authorization", user)).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/admin/news/" + article.getId()).header("Authorization", user))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.statusCode").value(403));
        assertThat(news.existsById(article.getId())).isTrue();
        mvc.perform(delete("/api/v1/admin/news/" + article.getId()).header("Authorization", login("admin", "admin123")))
                .andExpect(status().isOk());
        assertThat(news.existsById(article.getId())).isFalse();
    }

    @Test
    void uploadReadAndDeleteRespectMimeAndRoles() throws Exception {
        String user = login("author", "author123");
        String response = mvc.perform(multipart(UPLOAD).file(new MockMultipartFile("file", "photo.PNG", "text/html", PNG))
                        .header("Authorization", user))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.fileType").value("image/png"))
                .andReturn().getResponse().getContentAsString();
        JsonNode file = json.readTree(response).path("data");
        String filename = file.path("fileName").asText();
        Path stored = uploadDir.resolve(filename);
        assertThat(Files.readAllBytes(stored)).isEqualTo(PNG);
        mvc.perform(get(file.path("fileUrl").asText())).andExpect(status().isOk()).andExpect(content().bytes(PNG));
        mvc.perform(delete("/api/v1/files/" + filename).header("Authorization", user)).andExpect(status().isForbidden());
        assertThat(Files.exists(stored)).isTrue();
        mvc.perform(delete("/api/v1/files/" + filename).header("Authorization", login("admin", "admin123")))
                .andExpect(status().isOk());
        assertThat(Files.exists(stored)).isFalse();
        mvc.perform(get(file.path("fileUrl").asText())).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"photo.exe", "photo", "photo.jpg"})
    void invalidExtensionOrMismatchedContentIsRejected(String filename) throws Exception {
        mvc.perform(multipart(UPLOAD).file(new MockMultipartFile("file", filename, "image/png", PNG))
                        .header("Authorization", login("author", "author123")))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"jpg", "jpeg", "gif", "webp"})
    void supportedImageSignaturesKeepCorrectMime(String extension) throws Exception {
        byte[] bytes;
        if (extension.equals("webp")) {
            bytes = Base64.getDecoder().decode("UklGRiIAAABXRUJQVlA4IBYAAAAwAQCdASoBAAEADsD+JaQAA3AAAAAA");
        } else {
            var output = new ByteArrayOutputStream();
            ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB),
                    extension.equals("jpeg") ? "jpg" : extension, output);
            bytes = output.toByteArray();
        }
        mvc.perform(multipart(UPLOAD).file(new MockMultipartFile("file", "photo." + extension, "application/octet-stream", bytes))
                        .header("Authorization", login("author", "author123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fileType").value("image/" + (extension.equals("jpg") ? "jpeg" : extension)));
    }

    @Test
    void emptyFakeMissingAndOversizedFilesHaveClientErrors() throws Exception {
        String user = login("author", "author123");
        for (byte[] bytes : List.of(new byte[0], "not a PNG image".getBytes(StandardCharsets.UTF_8))) {
            mvc.perform(multipart(UPLOAD).file(new MockMultipartFile("file", "photo.png", "image/png", bytes))
                            .header("Authorization", user)).andExpect(status().isBadRequest());
        }
        mvc.perform(multipart(UPLOAD).header("Authorization", user)).andExpect(status().isBadRequest());
        mvc.perform(post(UPLOAD).contentType(MediaType.APPLICATION_JSON).content("{}").header("Authorization", user))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(multipart(UPLOAD).file(new MockMultipartFile("file", "large.png", "image/png", new byte[1025]))
                        .header("Authorization", user)).andExpect(status().isPayloadTooLarge());
        mvc.perform(multipart(UPLOAD).file(new MockMultipartFile("file", "photo.png", "image/png", PNG)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void actualServletEnforcesMultipartRequestLimit() throws Exception {
        String body = "--boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"large.png\"\r\n"
                + "Content-Type: image/png\r\n\r\n" + "x".repeat(3000) + "\r\n--boundary--\r\n";
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + UPLOAD))
                .timeout(Duration.ofSeconds(10)).header("Authorization", login("admin", "admin123"))
                .header("Content-Type", "multipart/form-data; boundary=boundary")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        var response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(413);
        assertThat(json.readTree(response.body()).path("statusCode").asInt()).isEqualTo(413);
    }

    @Test
    void failedWriteCleansPartialFileAndHidesInternalError() throws Exception {
        MockMultipartFile broken = new MockMultipartFile("file", "photo.png", "image/png", PNG) {
            @Override public InputStream getInputStream() {
                return new InputStream() {
                    private int read;
                    @Override public int read() throws IOException {
                        if (read == 12) throw new IOException("private-path-and-details");
                        return PNG[read++] & 0xff;
                    }
                };
            }
        };
        long before;
        try (var files = Files.list(uploadDir)) { before = files.count(); }
        mvc.perform(multipart(UPLOAD).file(broken).header("Authorization", login("admin", "admin123")))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private-path"))));
        try (var files = Files.list(uploadDir)) { assertThat(files.count()).isEqualTo(before); }
    }

    @Test
    void invalidDeleteCannotRemoveOtherFiles(@TempDir Path outsideDir) throws Exception {
        String admin = login("admin", "admin123");
        mvc.perform(delete("/api/v1/files/.gitkeep").header("Authorization", admin)).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/v1/files/missing.png").header("Authorization", admin)).andExpect(status().isNotFound());
        Path outside = Files.writeString(outsideDir.resolve("outside.png"), "keep");
        assertThatThrownBy(() -> storage.deleteFile(outside.toString())).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> storage.deleteFile(uploadDir.relativize(outside).toString())).isInstanceOf(BadRequestException.class);
        assertThat(Files.readString(outside)).isEqualTo("keep");
    }

    @Test
    void corsPreflightAllowsConfiguredOriginWithoutTokenAndRejectsOthers() throws Exception {
        mvc.perform(options(UPLOAD).header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options(UPLOAD).header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/news").header("Origin", "http://localhost:5173"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    private String login(String username, String password) throws Exception {
        String response = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(response).path("data").path("accessToken").asText();
    }
}
