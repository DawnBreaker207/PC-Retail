package com.dawn.user.infra.sms;

import com.dawn.user.port.out.SmsPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogSmsAdapter implements SmsPort {

    @Override
    public void send(String phone, String message) {
        // No SMS gateway yet; logs the code for dev/demo — replace with a real provider in prod
        log.info("SMS to {}: {}", phone, message);
    }
}
