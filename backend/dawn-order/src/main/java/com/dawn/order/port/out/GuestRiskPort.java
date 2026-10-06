package com.dawn.order.port.out;

public interface GuestRiskPort {
    boolean isPhoneVerified(String phone);

    /** Dùng 1 lần sau khi đơn tạo thành công. */
    void consumePhoneVerification(String phone);

    long phoneAbuseCount(String phone);

    void recordPhoneAbuse(String phone);
}
