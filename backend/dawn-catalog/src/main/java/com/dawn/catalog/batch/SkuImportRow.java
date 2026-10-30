package com.dawn.catalog.batch;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuImportRow {

    @ExcelProperty("SKU Code")
    private String skuCode;

    @ExcelProperty("Name")
    private String name;

    @ExcelProperty("Category")
    private String categoryName;

    @ExcelProperty("Brand")
    private String brandName;

    @ExcelProperty("Price")
    private BigDecimal price;

    @ExcelProperty("Cost Price")
    private BigDecimal costPrice;

    @ExcelProperty("Description")
    private String description;
}
