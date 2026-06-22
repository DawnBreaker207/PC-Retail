package com.dawn.user.port.out;

import com.dawn.user.dto.EmailMessage;

public interface EmailPublisherPort {
    void publish(EmailMessage message);
}
