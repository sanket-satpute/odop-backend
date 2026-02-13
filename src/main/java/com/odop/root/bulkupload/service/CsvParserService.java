package com.odop.root.bulkupload.service;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service for parsing CSV files
 */
@Service
@Slf4j
public class CsvParserService {
    
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_ROWS = 10000;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "text/csv",
            "application/csv",
            "text/plain",
            "application/vnd.ms-excel"
    );
    
    /**
     * Validate uploaded file
     */
    public ValidationResult validateFile(MultipartFile file) {
        ValidationResult result = new ValidationResult();
        
        // Check if file is empty
        if (file == null || file.isEmpty()) {
            result.addError("File is empty or not provided");
            return result;
        }
        
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            result.addError("File size exceeds maximum allowed (10MB)");
            return result;
        }
        
        // Check content type
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            // Also check file extension
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
                result.addError("Invalid file type. Only CSV files are allowed");
                return result;
            }
        }
        
        result.setValid(true);
        return result;
    }
    
    /**
     * Parse CSV file and return records
     */
    public ParseResult parseFile(MultipartFile file, boolean hasHeader, char delimiter) {
        ParseResult result = new ParseResult();
        List<Map<String, String>> records = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        
        try (Reader reader = new BufferedReader(new InputStreamReader(
                file.getInputStream(), StandardCharsets.UTF_8))) {
            
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setDelimiter(delimiter)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .setQuote('"')
                    .setSkipHeaderRecord(false)
                    .build();
            
            CSVParser parser = format.parse(reader);
            List<CSVRecord> csvRecords = parser.getRecords();
            
            if (csvRecords.isEmpty()) {
                result.addError("CSV file is empty");
                return result;
            }
            
            // Extract headers
            if (hasHeader) {
                CSVRecord headerRecord = csvRecords.get(0);
                for (int i = 0; i < headerRecord.size(); i++) {
                    String header = headerRecord.get(i).trim();
                    headers.add(header.isEmpty() ? "column_" + i : header);
                }
                csvRecords = csvRecords.subList(1, csvRecords.size());
            } else {
                // Generate default headers
                if (!csvRecords.isEmpty()) {
                    for (int i = 0; i < csvRecords.get(0).size(); i++) {
                        headers.add("column_" + i);
                    }
                }
            }
            
            // Check row count
            if (csvRecords.size() > MAX_ROWS) {
                result.addError("File exceeds maximum rows allowed (" + MAX_ROWS + ")");
                return result;
            }
            
            // Parse data rows
            int rowNum = hasHeader ? 2 : 1;
            for (CSVRecord record : csvRecords) {
                try {
                    Map<String, String> row = new LinkedHashMap<>();
                    for (int i = 0; i < headers.size(); i++) {
                        String value = i < record.size() ? record.get(i) : "";
                        row.put(headers.get(i), value);
                    }
                    row.put("_rowNumber", String.valueOf(rowNum));
                    records.add(row);
                } catch (Exception e) {
                    log.warn("Error parsing row {}: {}", rowNum, e.getMessage());
                    result.addWarning("Row " + rowNum + ": " + e.getMessage());
                }
                rowNum++;
            }
            
            result.setHeaders(headers);
            result.setRecords(records);
            result.setTotalRows(records.size());
            result.setSuccess(true);
            
        } catch (IOException e) {
            log.error("Error parsing CSV file", e);
            result.addError("Error reading CSV file: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Parse CSV with automatic delimiter detection
     */
    public ParseResult parseFileAutoDetect(MultipartFile file, boolean hasHeader) {
        char detectedDelimiter = detectDelimiter(file);
        return parseFile(file, hasHeader, detectedDelimiter);
    }
    
    /**
     * Detect CSV delimiter
     */
    private char detectDelimiter(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String firstLine = reader.readLine();
            if (firstLine != null) {
                int commas = countOccurrences(firstLine, ',');
                int semicolons = countOccurrences(firstLine, ';');
                int tabs = countOccurrences(firstLine, '\t');
                int pipes = countOccurrences(firstLine, '|');
                
                if (semicolons > commas && semicolons > tabs && semicolons > pipes) {
                    return ';';
                }
                if (tabs > commas && tabs > semicolons && tabs > pipes) {
                    return '\t';
                }
                if (pipes > commas && pipes > semicolons && pipes > tabs) {
                    return '|';
                }
            }
        } catch (IOException e) {
            log.warn("Could not detect delimiter, defaulting to comma", e);
        }
        return ',';
    }
    
    private int countOccurrences(String str, char c) {
        int count = 0;
        boolean inQuotes = false;
        for (char ch : str.toCharArray()) {
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == c && !inQuotes) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Generate sample CSV content
     */
    public String generateSampleCsv(List<String> headers, List<Map<String, String>> sampleData) {
        StringBuilder sb = new StringBuilder();
        
        // Headers
        sb.append(String.join(",", headers)).append("\n");
        
        // Data rows
        for (Map<String, String> row : sampleData) {
            List<String> values = new ArrayList<>();
            for (String header : headers) {
                String value = row.getOrDefault(header, "");
                // Quote values containing commas or quotes
                if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                    value = "\"" + value.replace("\"", "\"\"") + "\"";
                }
                values.add(value);
            }
            sb.append(String.join(",", values)).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Preview first N rows of CSV
     */
    public ParseResult previewFile(MultipartFile file, int maxRows, boolean hasHeader) {
        ParseResult fullResult = parseFileAutoDetect(file, hasHeader);
        
        if (!fullResult.isSuccess() || fullResult.getRecords() == null) {
            return fullResult;
        }
        
        ParseResult previewResult = new ParseResult();
        previewResult.setSuccess(true);
        previewResult.setHeaders(fullResult.getHeaders());
        previewResult.setTotalRows(fullResult.getTotalRows());
        
        List<Map<String, String>> preview = fullResult.getRecords().size() > maxRows
                ? fullResult.getRecords().subList(0, maxRows)
                : fullResult.getRecords();
        
        previewResult.setRecords(preview);
        previewResult.setPreviewCount(preview.size());
        
        return previewResult;
    }
    
    /**
     * Validation result
     */
    @Data
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }
        
        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
    }
    
    /**
     * Parse result
     */
    @Data
    public static class ParseResult {
        private boolean success;
        private List<String> headers;
        private List<Map<String, String>> records;
        private int totalRows;
        private int previewCount;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            this.success = false;
            this.errors.add(error);
        }
        
        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
    }
}
