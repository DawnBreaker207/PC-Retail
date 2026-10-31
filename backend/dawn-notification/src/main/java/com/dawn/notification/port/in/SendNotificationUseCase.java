package com.dawn.notification.port.in;

import com.dawn.notification.dto.NotificationEvent;

public interface SendNotificationUseCase {

    /**
     * Process an incoming notification event: persist a record and dispatch to the
     * appropriate channel (EMAIL, SMS, PUSH).
     */
    void processEvent(NotificationEvent event);
}
