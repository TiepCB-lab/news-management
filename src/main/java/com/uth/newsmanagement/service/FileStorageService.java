package com.uth.newsmanagement.service;

import com.uth.newsmanagement.common.exception.BadRequestException;
import com.uth.newsmanagement.common.exception.AppException;
import com.uth.newsmanagement.common.exception.ResourceNotFoundException;
import com.uth.newsmanagement.dto.file.FileUploadResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.nio.file.*;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;

/**
 * Xử lý lưu trữ và xóa file trên ổ đĩa vật lý (Nhiệm vụ Backend).
 */
@Service
public class FileStorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private Path fileStorageLocation;

    @Value("${spring.servlet.multipart.max-file-size:5MB}")
    private DataSize maxFileSize;

    private static final Map<String, String> IMAGE_TYPES = Map.of(
            "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png",
            "webp", "image/webp", "gif", "image/gif"
    );

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ file: " + uploadDir, ex);
        }
    }

    /**
     * Upload và lưu file vào thư mục uploads
     */
    public FileUploadResponse storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File gửi lên không được rỗng");
        }
        if (file.getSize() > maxFileSize.toBytes()) {
            throw new AppException("Dung lượng file vượt quá giới hạn cho phép", HttpStatus.PAYLOAD_TOO_LARGE);
        }
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        extension = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        if (!IMAGE_TYPES.containsKey(extension)) {
            throw new BadRequestException("Chỉ chấp nhận ảnh jpg, jpeg, png, webp hoặc gif");
        }
        Path target = null;
        try (BufferedInputStream input = new BufferedInputStream(file.getInputStream())) {
            input.mark(12);
            byte[] header = input.readNBytes(12);
            input.reset();
            if (!matchesSignature(extension, header)) {
                throw new BadRequestException("Nội dung file không khớp định dạng ảnh");
            }
            target = Files.createTempFile(fileStorageLocation, "image-", "." + extension);
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            String fileName = target.getFileName().toString();
            return new FileUploadResponse(
                    fileName,
                    "/uploads/" + fileName,
                    IMAGE_TYPES.get(extension),
                    file.getSize()
            );
        } catch (IOException ex) {
            if (target != null) {
                try { Files.deleteIfExists(target); }
                catch (IOException cleanupError) { ex.addSuppressed(cleanupError); }
            }
            throw new RuntimeException("Không thể lưu file", ex);
        }
    }

    private boolean matchesSignature(String extension, byte[] header) {
        // shortcut: Chỉ kiểm tra chữ ký file; cần decoder/quét nội dung khi triển khai công khai.
        String signature = HexFormat.of().formatHex(header);
        return switch (extension) {
            case "png" -> signature.startsWith("89504e470d0a1a0a");
            case "jpg", "jpeg" -> signature.startsWith("ffd8ff");
            case "gif" -> signature.startsWith("474946383761") || signature.startsWith("474946383961");
            case "webp" -> signature.startsWith("52494646") && signature.length() >= 24
                    && signature.substring(16, 24).equals("57454250");
            default -> false;
        };
    }

    /**
     * Xóa file khỏi thư mục uploads
     */
    public void deleteFile(String fileName) {
        if (fileName == null || !fileName.matches("[A-Za-z0-9_-]+\\.(jpg|jpeg|png|webp|gif)")) {
            throw new BadRequestException("Tên file không hợp lệ");
        }
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();

            // Đảm bảo không truy cập ra ngoài thư mục uploads (Path Traversal Protection)
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new BadRequestException("Tên file không hợp lệ");
            }

            if (!Files.isRegularFile(filePath, LinkOption.NOFOLLOW_LINKS) || !Files.deleteIfExists(filePath)) {
                throw new ResourceNotFoundException("File", "fileName", fileName);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Không thể xóa file: " + fileName, ex);
        }
    }
}
