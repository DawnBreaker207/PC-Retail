package com.dawn.catalog.search;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(name = "org.opensearch.client.RestHighLevelClient")
@ConditionalOnProperty(name = "app.opensearch.enabled", havingValue = "true", matchIfMissing = false)
public class OpenSearchConfig {

    @Value("${app.opensearch.host:localhost}")
    private String host;

    @Value("${app.opensearch.port:9200}")
    private int port;

    @Bean
    public RestHighLevelClient openSearchClient() {
        log.info("Initializing OpenSearch client at {}:{}", host, port);
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, "http")));
    }
}
