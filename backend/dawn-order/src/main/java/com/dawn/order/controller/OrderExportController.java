package com.dawn.order.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderExportController {

    private static final String EXPORT_SQL = """
            SELECT o.id, o.user_id, oi.sku_id, oi.product_name,
                   oi.quantity, oi.unit_price, oi.total_price, o.status, o.created_at
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            ORDER BY o.id ASC
            """;

    private static final String[] HEADERS = {
            "Order ID", "Customer ID", "SKU ID", "Product Name",
            "Quantity", "Unit Price", "Total Price", "Status", "Created At"
    };

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/export-excel")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public void exportOrders(
            @RequestParam(defaultValue = "xlsx") String format,
            HttpServletResponse response) throws IOException {

        log.info("Starting streaming order export format={}", format);
        if ("csv".equalsIgnoreCase(format)) {
            exportCsv(response);
        } else {
            exportXlsx(response);
        }
    }

    private void exportXlsx(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"orders_export.xlsx\"");

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Orders");

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col++) {
                headerRow.createCell(col).setCellValue(HEADERS[col]);
            }

            AtomicInteger rowIdx = new AtomicInteger(1);
            jdbcTemplate.query(EXPORT_SQL, rs -> {
                Row row = sheet.createRow(rowIdx.getAndIncrement());
                row.createCell(0).setCellValue(rs.getLong("id"));
                row.createCell(1).setCellValue(rs.getLong("user_id"));
                row.createCell(2).setCellValue(rs.getLong("sku_id"));
                row.createCell(3).setCellValue(rs.getString("product_name"));
                row.createCell(4).setCellValue(rs.getInt("quantity"));
                row.createCell(5).setCellValue(rs.getDouble("unit_price"));
                row.createCell(6).setCellValue(rs.getDouble("total_price"));
                row.createCell(7).setCellValue(rs.getString("status"));
                row.createCell(8).setCellValue(String.valueOf(rs.getTimestamp("created_at")));
                if (rowIdx.get() % 100_000 == 0) {
                    log.info("Exported {} rows...", rowIdx.get());
                }
            });

            workbook.write(response.getOutputStream());
            response.flushBuffer();
            workbook.dispose();
            log.info("Finished XLSX export of {} rows", rowIdx.get() - 1);
        }
    }

    private void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"orders_export.csv\"");

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write('﻿');
            writer.write("Order ID,Customer ID,SKU ID,Product Name,Quantity,Unit Price,Total Price,Status,Created At\r\n");

            AtomicInteger count = new AtomicInteger(0);
            jdbcTemplate.query(EXPORT_SQL, rs -> {
                try {
                    writer.write(String.format("%d,%d,%d,\"%s\",%d,%.2f,%.2f,%s,%s\r\n",
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getLong("sku_id"),
                            rs.getString("product_name").replace("\"", "\"\""),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("total_price"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at")));
                    if (count.incrementAndGet() % 200_000 == 0) {
                        writer.flush();
                        log.info("Exported {} rows...", count.get());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.flush();
            log.info("Finished CSV export of {} rows", count.get());
        }
    }
}
