package com.dawn.order.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.invoice")
@Getter
@Setter
public class InvoiceConfig {
    private String sellerName = "Công ty TNHH PC Retail";
    private String sellerTaxCode;
    private String sellerAddress;
    private double vatRate = 0.10;
}
