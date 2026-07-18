package com.dawn.ai.controller;

import com.dawn.ai.controller.request.ChatRequest;
import com.dawn.ai.dto.AiSearchResult;
import com.dawn.ai.port.in.SearchProductUseCase;
import com.dawn.ai.port.out.LlmPort;
import com.dawn.common.response.ResponseObject;
import com.dawn.common.security.AuthorizationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {
    private final SearchProductUseCase searchProductUseCase;
    private final LlmPort llmPort;

    @GetMapping("/search")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ResponseObject<List<AiSearchResult>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseObject.success(searchProductUseCase.search(q, Math.min(limit, 10)));
    }

    @PostMapping("/chat")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ResponseObject<String> chat(@RequestBody ChatRequest req) {
        return ResponseObject.success(llmPort.chat(req.message()));
    }
}
