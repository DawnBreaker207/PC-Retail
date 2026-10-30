package com.dawn.catalog.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.MultiMatchQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnBean(RestHighLevelClient.class)
@RequiredArgsConstructor
public class OpenSearchIndexService {

    private static final String INDEX = "skus";

    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "product-events", groupId = "dawn-catalog-opensearch")
    public void onProductEvent(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            long skuId = node.path("skuId").asLong(0);
            if (skuId == 0) return;

            Map<String, Object> doc = new HashMap<>();
            doc.put("skuId", skuId);
            doc.put("skuCode", node.path("skuCode").asText(""));
            doc.put("name", node.path("name").asText(""));
            doc.put("description", node.path("description").asText(""));
            doc.put("categoryId", node.path("categoryId").asLong(0));
            doc.put("price", node.path("price").asDouble(0));

            IndexRequest request = new IndexRequest(INDEX).id(String.valueOf(skuId)).source(doc);
            client.index(request, RequestOptions.DEFAULT);
            log.debug("Indexed SKU {} to OpenSearch", skuId);
        } catch (Exception e) {
            log.error("Failed to index to OpenSearch from product-event", e);
        }
    }

    public List<Long> search(String query, int size) {
        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                    .query(QueryBuilders.multiMatchQuery(query, "name", "description", "skuCode")
                            .type(MultiMatchQueryBuilder.Type.BEST_FIELDS))
                    .size(size);

            SearchRequest request = new SearchRequest(INDEX).source(sourceBuilder);
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            List<Long> ids = new ArrayList<>();
            response.getHits().forEach(hit -> ids.add(Long.parseLong(hit.getId())));
            return ids;
        } catch (IOException e) {
            log.error("OpenSearch search failed for query '{}'", query, e);
            return List.of();
        }
    }
}
