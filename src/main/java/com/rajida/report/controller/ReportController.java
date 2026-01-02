package com.rajida.report.controller;

import com.rajida.report.model.ApiResponse;
import com.rajida.report.model.ReportData;
import com.rajida.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Upload JRXML file
     * POST /api/reports/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadJrxml(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = reportService.uploadJrxmlFile(file);
            Map<String, String> data = new HashMap<>();
            data.put("fileName", fileName);
            data.put("message", "File uploaded successfully");

            return ResponseEntity.ok(new ApiResponse(true, "JRXML file uploaded successfully", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * Get list of uploaded JRXML files
     * GET /api/reports/list
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse> listJrxmlFiles() {
        try {
            String[] files = reportService.getUploadedFiles();
            return ResponseEntity.ok(new ApiResponse(true, "Files retrieved successfully updated", files));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve files: " + e.getMessage()));
        }
    }

    /**
     * Delete JRXML file
     * DELETE /api/reports/delete/{fileName}
     */
    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<ApiResponse> deleteJrxml(@PathVariable String fileName) {
        try {
            boolean deleted = reportService.deleteJrxmlFile(fileName);
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse(true, "File deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Delete failed: " + e.getMessage()));
        }
    }

    /**
     * Export JRXML to PDF with data
     * POST /api/reports/export-pdf-with-data
     */
    @PostMapping("/export-pdf-with-data")
    public ResponseEntity<?> exportToPdfWithData(@RequestBody ReportData reportData) {
        try {
            byte[] pdfBytes = reportService.exportToPdfWithData(
                    reportData.getJrxmlFileName(),
                    reportData.getParameters() != null ? reportData.getParameters() : new HashMap<>(),
                    reportData.getDataRecords());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "PDF export failed: " + e.getMessage()));
        }
    }

    /**
     * Export JRXML to HTML with data
     * POST /api/reports/export-html-with-data
     */
    @PostMapping("/export-html-with-data")
    public ResponseEntity<?> exportToHtmlWithData(@RequestBody ReportData reportData) {
        try {
            String htmlContent = reportService.exportToHtmlWithData(
                    reportData.getJrxmlFileName(),
                    reportData.getParameters() != null ? reportData.getParameters() : new HashMap<>(),
                    reportData.getDataRecords());

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlContent);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "HTML export failed: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * GET /api/reports/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse> health() {
        return ResponseEntity.ok(new ApiResponse(true, "Service is running"));
    }
}
