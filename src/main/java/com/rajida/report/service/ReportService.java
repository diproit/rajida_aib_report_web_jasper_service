package com.rajida.report.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    /**
     * Configure JasperReports to handle missing fonts gracefully
     */
    static {
        // Ignore missing fonts and use default font
        System.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
        System.setProperty("net.sf.jasperreports.default.font.name", "DejaVuSans");
        // For HTML export
        System.setProperty("net.sf.jasperreports.html.skip.page", "false");
        // UTF-8 encoding for Unicode support
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        // Enable font embedding in PDF for better Unicode support
        System.setProperty("net.sf.jasperreports.pdf.font.embedded", "true");
        // Use DejaVuSans as fallback for missing fonts
        System.setProperty("net.sf.jasperreports.awt.detect.greek.text", "true");
    }

    /**
     * Upload JRXML file to server
     * If file already exists, returns success without re-uploading
     */
    public String uploadJrxmlFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!file.getOriginalFilename().endsWith(".jrxml")) {
            throw new IllegalArgumentException("File must be a JRXML file");
        }

        // Create uploads directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        // Get file name and path
        String fileName = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Always save/overwrite file (delete if exists first)
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // Save file
        file.transferTo(filePath.toFile());

        return fileName;
    }

    /**
     * Export JRXML to PDF with data
     */
    public byte[] exportToPdfWithData(String jrxmlFileName, Map<String, Object> parameters,
            List<Map<String, Object>> dataRecords) throws Exception {
        File jrxmlFile = new File(Paths.get(uploadDir, jrxmlFileName).toString());

        if (!jrxmlFile.exists()) {
            throw new FileNotFoundException("JRXML file not found: " + jrxmlFileName);
        }

        // Compile JRXML to JasperReport
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlFile.getAbsolutePath());

        // Prepare parameters with font handling
        Map<String, Object> reportParams = parameters != null ? parameters : new HashMap<>();
        reportParams.put(JRParameter.IS_IGNORE_PAGINATION, false);

        // Convert data records to correct types
        List<Map<String, Object>> convertedRecords = convertDataRecords(dataRecords, jasperReport);

        // Create data source from records
        JRDataSource dataSource;
        if (convertedRecords != null && !convertedRecords.isEmpty()) {
            dataSource = new JRMapArrayDataSource(convertedRecords.toArray(new Map[0]));
        } else {
            dataSource = new JREmptyDataSource();
        }

        // Fill report with data - catch font errors and retry with fallback
        JasperPrint jasperPrint;
        try {
            jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    reportParams,
                    dataSource);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Font")) {
                // Font error - enable font ignore and retry
                System.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
                System.setProperty("java.awt.headless", "true");
                jasperPrint = JasperFillManager.fillReport(
                        jasperReport,
                        reportParams,
                        dataSource);
            } else {
                throw e;
            }
        }

        // Export to PDF using modern API with Unicode support
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new net.sf.jasperreports.export.SimpleOutputStreamExporterOutput(pdfOutputStream));

        // Configure PDF export properties for Unicode/Sinhala support
        net.sf.jasperreports.export.SimplePdfExporterConfiguration exportConfig = new net.sf.jasperreports.export.SimplePdfExporterConfiguration();
        exportConfig.setMetadataTitle("Report");
        exportConfig.setMetadataAuthor("Rajida Report Service");
        exportConfig.setCompressed(true);
        exporter.setConfiguration(exportConfig);

        exporter.exportReport();

        return pdfOutputStream.toByteArray();
    }

    /**
     * Convert data records - normalize numeric types for JasperReports
     */
    private List<Map<String, Object>> convertDataRecords(List<Map<String, Object>> dataRecords,
            JasperReport jasperReport) {
        List<Map<String, Object>> convertedRecords = new java.util.ArrayList<>();

        // Build field type map
        Map<String, String> fieldTypeMap = new HashMap<>();
        JRField[] fields = jasperReport.getFields();
        if (fields != null) {
            for (JRField field : fields) {
                fieldTypeMap.put(field.getName(), field.getValueClassName());
            }
        }

        for (Map<String, Object> record : dataRecords) {
            Map<String, Object> convertedRecord = new HashMap<>();

            for (String fieldName : record.keySet()) {
                Object value = record.get(fieldName);

                // Try to convert based on field type
                String fieldClass = fieldTypeMap.get(fieldName);

                if (fieldClass != null && value != null) {
                    try {
                        if ("java.lang.Double".equals(fieldClass) && !(value instanceof Double)) {
                            // Convert to Double
                            if (value instanceof Number) {
                                value = ((Number) value).doubleValue();
                            } else if (value instanceof String) {
                                value = Double.parseDouble((String) value);
                            }
                        } else if ("java.lang.Integer".equals(fieldClass) && !(value instanceof Integer)) {
                            // Convert to Integer
                            if (value instanceof Number) {
                                value = ((Number) value).intValue();
                            } else if (value instanceof String) {
                                value = Integer.parseInt((String) value);
                            }
                        } else if ("java.lang.Long".equals(fieldClass) && !(value instanceof Long)) {
                            // Convert to Long
                            if (value instanceof Number) {
                                value = ((Number) value).longValue();
                            } else if (value instanceof String) {
                                value = Long.parseLong((String) value);
                            }
                        } else if ("java.sql.Timestamp".equals(fieldClass) && !(value instanceof Timestamp)) {
                            // Convert to Timestamp from ISO date string
                            if (value instanceof String) {
                                String dateStr = (String) value;
                                // Parse ISO format: 2022-02-22T00:00:00 or with milliseconds
                                LocalDateTime ldt = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                                value = Timestamp.valueOf(ldt);
                            } else if (value instanceof java.util.Date) {
                                value = new Timestamp(((java.util.Date) value).getTime());
                            }
                        }
                    } catch (Exception e) {
                        // If conversion fails, keep original value
                        System.err.println("Failed to convert field " + fieldName + ": " + e.getMessage());
                    }
                }

                convertedRecord.put(fieldName, value);
            }

            convertedRecords.add(convertedRecord);
        }

        return convertedRecords;
    }

    /**
     * Export JRXML to HTML with data
     */
    public String exportToHtmlWithData(String jrxmlFileName, Map<String, Object> parameters,
            List<Map<String, Object>> dataRecords) throws Exception {
        System.out.println("DEBUG HTML EXPORT STARTED: jrxmlFileName=" + jrxmlFileName);
        File jrxmlFile = new File(Paths.get(uploadDir, jrxmlFileName).toString());

        if (!jrxmlFile.exists()) {
            throw new FileNotFoundException("JRXML file not found: " + jrxmlFileName);
        }

        // Compile JRXML to JasperReport
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlFile.getAbsolutePath());

        // Debug: Log field information
        JRField[] fields = jasperReport.getFields();
        System.out.println("DEBUG HTML EXPORT: Total fields in JRXML: " + (fields != null ? fields.length : 0));
        if (fields != null && fields.length > 0) {
            System.out.println("DEBUG HTML EXPORT: First 5 fields: " +
                    String.join(", ", java.util.Arrays.stream(fields).limit(5).map(f -> f.getName())
                            .collect(java.util.stream.Collectors.toList())));
        }

        // Prepare parameters with font handling
        Map<String, Object> reportParams = parameters != null ? parameters : new HashMap<>();
        reportParams.put(JRParameter.IS_IGNORE_PAGINATION, false);

        // Convert data records to correct types
        List<Map<String, Object>> convertedRecords = convertDataRecords(dataRecords, jasperReport);

        // Log data source info
        System.out.println("DEBUG: Original dataRecords size: " + (dataRecords != null ? dataRecords.size() : "null"));
        System.out.println(
                "DEBUG: Converted records size: " + (convertedRecords != null ? convertedRecords.size() : "null"));
        if (convertedRecords != null && !convertedRecords.isEmpty()) {
            System.out.println("DEBUG: First record keys: " + convertedRecords.get(0).keySet());
            System.out.println("DEBUG: First record full_name_ln1: " + convertedRecords.get(0).get("full_name_ln1"));
            System.out
                    .println("DEBUG: First record closing_balance: " + convertedRecords.get(0).get("closing_balance"));
        }

        // Create data source from records
        JRDataSource dataSource;
        if (convertedRecords != null && !convertedRecords.isEmpty()) {
            // Debug: Verify all fields exist in data
            JRField[] jrFields = jasperReport.getFields();
            Map<String, Object> firstRecord = convertedRecords.get(0);
            System.out.println("DEBUG HTML EXPORT: Checking field-data mapping for " + jrFields.length + " fields:");
            for (JRField field : jrFields) {
                boolean exists = firstRecord.containsKey(field.getName());
                if (!exists || field.getName().equals("full_name_ln1") || field.getName().equals("closing_balance")
                        || field.getName().equals("branch_month_id")) {
                    System.out.println("  - Field '" + field.getName() + "': " + (exists ? "✓" : "✗ MISSING"));
                }
            }
            dataSource = new JRMapArrayDataSource(convertedRecords.toArray(new Map[0]));
        } else {
            dataSource = new JREmptyDataSource();
        }

        // Fill report with data - catch font errors and retry with fallback
        JasperPrint jasperPrint;
        try {
            System.out.println("DEBUG HTML EXPORT: Starting to fill report with "
                    + (convertedRecords != null ? convertedRecords.size() : 0) + " records");
            jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    reportParams,
                    dataSource);
            System.out
                    .println("DEBUG HTML EXPORT: Report filled successfully. Pages: " + jasperPrint.getPages().size());
        } catch (Exception e) {
            System.err.println("DEBUG HTML EXPORT: Error during fill: " + e.getMessage());
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("Font")) {
                // Font error - enable font ignore and retry
                System.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
                System.setProperty("java.awt.headless", "true");
                jasperPrint = JasperFillManager.fillReport(
                        jasperReport,
                        reportParams,
                        dataSource);
            } else {
                throw e;
            }
        }

        // Export to HTML using modern API
        StringWriter htmlWriter = new StringWriter();
        HtmlExporter exporter = new HtmlExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleHtmlExporterOutput(htmlWriter));
        exporter.exportReport();

        String result = htmlWriter.toString();
        System.out.println("DEBUG HTML EXPORT: Export complete. HTML size: " + result.length() + " bytes");
        return result;
    }

    /**
     * Get list of uploaded JRXML files
     */
    public String[] getUploadedFiles() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            return new String[0];
        }

        return Files.list(uploadPath)
                .filter(path -> path.toString().endsWith(".jrxml"))
                .map(path -> path.getFileName().toString())
                .toArray(String[]::new);
    }

    /**
     * Delete JRXML file
     */
    public boolean deleteJrxmlFile(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir, fileName);
        return Files.deleteIfExists(filePath);
    }
}
