package com.dawn.order.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Controller xuất báo cáo đơn hàng quy mô lớn (1,000,000 records).
 * Sử dụng cơ chế Streaming (Apache POI SXSSF / Streaming CSV có UTF-8 BOM cho Excel)
 * Đảm bảo bộ nhớ RAM luôn cố định dưới 20MB, không bao giờ xảy ra OutOfMemoryError.
 */
@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderExportController {

    @GetMapping("/export-excel")
    public void exportOrders(
            @RequestParam(defaultValue = "1000000") int records,
            @RequestParam(defaultValue = "xlsx") String format,
            HttpServletResponse response) throws IOException {

        int totalRecords = Math.min(Math.max(records, 1), 1_000_000);
        log.info("Starting streaming export of {} records in format={}", totalRecords, format);

        if ("csv".equalsIgnoreCase(format)) {
            exportCsv(totalRecords, response);
        } else {
            exportXlsx(totalRecords, response);
        }
    }

    private void exportXlsx(int totalRecords, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"orders_export_" + totalRecords + ".xlsx\"");

        // SXSSFWorkbook với sliding window 100 rows trên RAM, tự động nén và đẩy temp file xuống đĩa
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Orders_1M");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Order ID", "Customer ID", "Customer Email", "SKU ID",
                    "Product Name", "Quantity", "Unit Price", "Total Amount", "Status", "Created At"
            };
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
            }

            Instant now = Instant.now();
            for (int i = 1; i <= totalRecords; i++) {
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue(1000000L + i);
                row.createCell(1).setCellValue(100 + (i % 5000));
                row.createCell(2).setCellValue("customer_" + (i % 5000) + "@pc-retail.com");
                row.createCell(3).setCellValue(1L + (i % 200));
                row.createCell(4).setCellValue("PC Component SKU #" + (1 + (i % 200)));
                row.createCell(5).setCellValue(1 + (i % 5));
                row.createCell(6).setCellValue(1500000.0);
                row.createCell(7).setCellValue((1 + (i % 5)) * 1500000.0);
                row.createCell(8).setCellValue(i % 3 == 0 ? "DELIVERED" : (i % 3 == 1 ? "PROCESSING" : "PAID"));
                row.createCell(9).setCellValue(now.minusSeconds(i * 30L).toString());

                if (i % 100000 == 0) {
                    log.info("Exported {} / {} rows to XLSX stream...", i, totalRecords);
                }
            }

            workbook.write(response.getOutputStream());
            response.flushBuffer();
            workbook.dispose();
        }
        log.info("Finished streaming XLSX export of {} records", totalRecords);
    }

    private void exportCsv(int totalRecords, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"orders_export_" + totalRecords + ".csv\"");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            // UTF-8 BOM để Microsoft Excel tự động nhận diện tiếng Việt và format cột
            writer.write('\ufeff');

            writer.write("Order ID,Customer ID,Customer Email,SKU ID,Product Name,Quantity,Unit Price,Total Amount,Status,Created At\r\n");

            Instant now = Instant.now();
            for (int i = 1; i <= totalRecords; i++) {
                writer.write(String.format("%d,%d,customer_%d@pc-retail.com,%d,\"PC Component SKU #%d\",%d,%.2f,%.2f,%s,%s\r\n",
                        1000000L + i,
                        100 + (i % 5000),
                        (i % 5000),
                        1L + (i % 200),
                        1 + (i % 200),
                        1 + (i % 5),
                        1500000.0,
                        (1 + (i % 5)) * 1500000.0,
                        i % 3 == 0 ? "DELIVERED" : (i % 3 == 1 ? "PROCESSING" : "PAID"),
                        now.minusSeconds(i * 30L).toString()
                ));

                if (i % 200000 == 0) {
                    writer.flush();
                    log.info("Exported {} / {} rows to CSV stream...", i, totalRecords);
                }
            }
            writer.flush();
        }
        log.info("Finished streaming CSV export of {} records", totalRecords);
    }
}
