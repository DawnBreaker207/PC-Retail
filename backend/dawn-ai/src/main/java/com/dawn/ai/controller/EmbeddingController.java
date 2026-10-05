package com.dawn.ai.controller;

import com.dawn.ai.port.out.EmbeddingPort;
import com.dawn.common.response.ResponseObject;
import com.dawn.common.security.AuthorizationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/embeddings")
@RequiredArgsConstructor
public class EmbeddingController {
    private final EmbeddingPort embeddingPort;

    @PostMapping("/generate")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<String> generateAll() {
        Thread t = new Thread(() -> {
            try {
                embeddingPort.generateBatch();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(EmbeddingController.class)
                        .error("Embedding batch failed", e);
            }
        }, "embedding-batch");
        t.setDaemon(true);
        t.start();
        return ResponseObject.success("Batch embedding đã được khởi động. Kiểm tra log để theo dõi tiến trình.");
    }

    @PostMapping("/{skuId}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<String> generateOne(@PathVariable Long skuId) {
        embeddingPort.generateAndSave(skuId);
        return ResponseObject.success("Đã generate embedding cho SKU " + skuId);
    }
}
