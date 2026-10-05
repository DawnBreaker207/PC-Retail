package com.dawn.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ApiExceptionHandlerTest {

    ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    @DisplayName("Body JSON sai -> 400, không lộ chi tiết nội bộ")
    void malformedBody_badRequest() {
        var res = handler.handlerNotReadable(null);

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(res.getBody().getMessage()).isEqualTo("Malformed request body");
    }

    @Test
    @DisplayName("Vi phạm FK/unique -> 400, message an toàn")
    void dataIntegrity_badRequest() {
        var res = handler.handlerDataIntegrity(null);

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("Route không tồn tại -> 404")
    void noResource_notFound() {
        var res = handler.handlerNoResource(null);

        assertThat(res.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("Redis chết -> 503 để client retry sau")
    void redisDown_serviceUnavailable() {
        var res = handler.handlerRedisDown(null);

        assertThat(res.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    @DisplayName("Xung đột ghi đồng thời -> 409 để client retry")
    void optimisticLock_conflict() {
        var res = handler.handlerOptimisticLock(null);

        assertThat(res.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    @DisplayName("Lỗi lạ -> 500 message chung chung")
    void unknown_internalServerError() {
        var res = handler.handleAllException(new RuntimeException("boom"));

        assertThat(res.getStatusCode().value()).isEqualTo(500);
        assertThat(res.getBody().getMessage()).isEqualTo("Internal server error");
    }
}
