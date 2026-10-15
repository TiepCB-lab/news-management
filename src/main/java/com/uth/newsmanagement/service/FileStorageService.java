package com.uth.newsmanagement.service;

import com.uth.newsmanagement.common.exception.BadRequestException;
import com.uth.newsmanagement.common.exception.ResourceNotFoundException;
import com.uth.newsmanagement.dto.file.FileUploadResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Xử lý lưu trữ và xóa file trên ổ đĩa vật lý (Nhiệm vụ Backend).
 */
@Service
public class FileStorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private Path fileStorageLocation;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp", ".gif"
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

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String fileExtension = "";
        int extIndex = originalFilename.lastIndexOf(".");
        if (extIndex > 0) {
            fileExtension = originalFilename.substring(extIndex).toLowerCase();
        }

        // Validate đuôi file
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new BadRequestException("Định dạng file không được hỗ trợ. Chỉ chấp nhận: " + ALLOWED_EXTENSIONS);
        }

        // Sinh tên file duy nhất tránh xung đột
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String fileDownloadUri = "/uploads/" + uniqueFileName;

            return new FileUploadResponse(
                    uniqueFileName,
                    fileDownloadUri,
                    file.getContentType(),
                    file.getSize()
            );
        } catch (IOException ex) {
            throw new RuntimeException("Lỗi khi lưu trữ file " + originalFilename + ". Vui lòng thử lại!", ex);
        }
    }

    /**
     * Xóa file khỏi thư mục uploads
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();

            // Đảm bảo không truy cập ra ngoài thư mục uploads (Path Traversal Protection)
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new BadRequestException("Tên file không hợp lệ");
            }

            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException("File", "fileName", fileName);
            }

            Files.delete(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Không thể xóa file: " + fileName, ex);
        }
    }
}
