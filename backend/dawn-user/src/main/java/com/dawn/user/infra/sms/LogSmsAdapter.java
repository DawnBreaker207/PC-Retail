package com.dawn.user.infra.sms;

import com.dawn.user.port.out.SmsPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogSmsAdapter implements SmsPort {

    @Override
    public void send(String phone, String message) {
        // Chưa có SMS gateway: ghi log để dev/demo lấy mã. PROD phải thay bằng provider thật.
        log.info("SMS to {}: {}", phone, message);
    }
}
