package com.dawn.user.port.out;

public interface SmsPort {
    /**
     * Gửi SMS. Chưa có nhà cung cấp thật — LogSmsAdapter ghi log.
     * Khi ký HĐ SMS gateway: implement interface này, không đụng service.
     */
    void send(String phone, String message);
}
