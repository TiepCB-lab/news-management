package com.uth.newsmanagement.controller;

import com.uth.newsmanagement.common.dto.ApiResponse;
import com.uth.newsmanagement.dto.file.FileUploadResponse;
import com.uth.newsmanagement.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller Quản lý File: Upload & Xóa file (Yêu cầu đề bài).
 * Cần Bearer Token để thực hiện.
 */
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * API Upload file (hình ảnh thumbnail, minh họa tin tức)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(@RequestParam("file") MultipartFile file) {
        FileUploadResponse responseData = fileStorageService.storeFile(file);
        ApiResponse<FileUploadResponse> response = ApiResponse.created(responseData, "Upload file thành công");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API Xóa file vật lý
     */
    @DeleteMapping("/{fileName}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName);
        ApiResponse<Void> response = ApiResponse.success(null, "Xóa file thành công");
        return ResponseEntity.ok(response);
    }
}
