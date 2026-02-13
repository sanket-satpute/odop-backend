package com.odop.root.report.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for generating Excel reports
 */
@Service
@Slf4j
public class ExcelExportService {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    
    /**
     * Generate Excel file from data
     */
    public byte[] generateExcel(String sheetName, List<String> headers, List<List<Object>> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(sheetName);
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (List<Object> rowData : rows) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.size(); i++) {
                    Cell cell = row.createCell(i);
                    Object value = rowData.get(i);
                    setCellValue(cell, value, dataStyle, currencyStyle, dateStyle);
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Freeze header row
            sheet.createFreezePane(0, 1);
            
            // Enable auto-filter
            sheet.setAutoFilter(new CellRangeAddress(0, rows.size(), 0, headers.size() - 1));
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            log.error("Error generating Excel", e);
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }
    
    /**
     * Generate Excel with multiple sheets
     */
    public byte[] generateMultiSheetExcel(Map<String, SheetData> sheets) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle summaryStyle = createSummaryStyle(workbook);
            
            for (Map.Entry<String, SheetData> entry : sheets.entrySet()) {
                String sheetName = entry.getKey();
                SheetData data = entry.getValue();
                
                XSSFSheet sheet = workbook.createSheet(sheetName);
                
                // Add title if present
                int startRow = 0;
                if (data.getTitle() != null) {
                    Row titleRow = sheet.createRow(startRow++);
                    Cell titleCell = titleRow.createCell(0);
                    titleCell.setCellValue(data.getTitle());
                    titleCell.setCellStyle(summaryStyle);
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, data.getHeaders().size() - 1));
                    startRow++; // Empty row after title
                }
                
                // Add summary if present
                if (data.getSummary() != null && !data.getSummary().isEmpty()) {
                    for (Map.Entry<String, Object> summaryEntry : data.getSummary().entrySet()) {
                        Row summaryRow = sheet.createRow(startRow++);
                        Cell keyCell = summaryRow.createCell(0);
                        keyCell.setCellValue(summaryEntry.getKey() + ":");
                        keyCell.setCellStyle(summaryStyle);
                        
                        Cell valueCell = summaryRow.createCell(1);
                        setCellValue(valueCell, summaryEntry.getValue(), dataStyle, currencyStyle, dateStyle);
                    }
                    startRow++; // Empty row after summary
                }
                
                // Create header row
                Row headerRow = sheet.createRow(startRow++);
                for (int i = 0; i < data.getHeaders().size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(data.getHeaders().get(i));
                    cell.setCellStyle(headerStyle);
                }
                
                // Create data rows
                for (List<Object> rowData : data.getRows()) {
                    Row row = sheet.createRow(startRow++);
                    for (int i = 0; i < rowData.size(); i++) {
                        Cell cell = row.createCell(i);
                        Object value = rowData.get(i);
                        setCellValue(cell, value, dataStyle, currencyStyle, dateStyle);
                    }
                }
                
                // Auto-size columns
                for (int i = 0; i < data.getHeaders().size(); i++) {
                    sheet.autoSizeColumn(i);
                }
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            log.error("Error generating multi-sheet Excel", e);
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }
    
    /**
     * Generate sales summary report
     */
    public byte[] generateSalesSummaryReport(
            String title,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Map<String, Object> summary,
            List<Map<String, Object>> salesData) {
        
        Map<String, SheetData> sheets = new LinkedHashMap<>();
        
        // Summary sheet
        SheetData summarySheet = new SheetData();
        summarySheet.setTitle(title);
        summarySheet.setSummary(summary);
        summarySheet.setHeaders(List.of("Metric", "Value"));
        summarySheet.setRows(summary.entrySet().stream()
                .map(e -> List.<Object>of(e.getKey(), e.getValue()))
                .toList());
        sheets.put("Summary", summarySheet);
        
        // Details sheet
        if (!salesData.isEmpty()) {
            SheetData detailsSheet = new SheetData();
            detailsSheet.setTitle("Sales Details");
            
            List<String> headers = new ArrayList<>(salesData.get(0).keySet());
            detailsSheet.setHeaders(headers);
            
            List<List<Object>> rows = salesData.stream()
                    .map(row -> headers.stream().map(row::get).toList())
                    .toList();
            detailsSheet.setRows(rows);
            
            sheets.put("Details", detailsSheet);
        }
        
        return generateMultiSheetExcel(sheets);
    }
    
    // ==================== STYLES ====================
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("₹#,##0.00"));
        return style;
    }
    
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd-mm-yyyy"));
        return style;
    }
    
    private CellStyle createSummaryStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
    private void setCellValue(Cell cell, Object value, CellStyle dataStyle, 
                              CellStyle currencyStyle, CellStyle dateStyle) {
        if (value == null) {
            cell.setCellValue("");
            cell.setCellStyle(dataStyle);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
            // Check if it looks like currency
            if (value instanceof Double && ((Double) value) > 100) {
                cell.setCellStyle(currencyStyle);
            } else {
                cell.setCellStyle(dataStyle);
            }
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).format(DATETIME_FORMAT));
            cell.setCellStyle(dateStyle);
        } else if (value instanceof LocalDate) {
            cell.setCellValue(((LocalDate) value).format(DATE_FORMAT));
            cell.setCellStyle(dateStyle);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
            cell.setCellStyle(dateStyle);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value ? "Yes" : "No");
            cell.setCellStyle(dataStyle);
        } else {
            cell.setCellValue(value.toString());
            cell.setCellStyle(dataStyle);
        }
    }
    
    /**
     * Sheet data holder
     */
    @lombok.Data
    public static class SheetData {
        private String title;
        private Map<String, Object> summary;
        private List<String> headers;
        private List<List<Object>> rows;
    }
}
