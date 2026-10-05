package com.dawn.payment.infra.vnpay;

import com.dawn.common.message.Message;
import com.dawn.payment.dto.CreatePaymentResult;
import com.dawn.payment.dto.PaymentDto;
import com.dawn.payment.dto.VnpayQueryResult;
import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentMethod;
import com.dawn.payment.entity.enums.PaymentResultStatus;
import com.dawn.payment.infra.gateway.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VnpayGatewayAdapter implements PaymentGateway {

    @Value("${payment.VNPay.url}")
    private String vnp_PayUrl;

    @Value("${payment.VNPay.queryUrl}")
    private String vnp_QueryUrl;

    @Value("${payment.VNPay.returnUrl}")
    private String vnp_ReturnUrl;

    @Value("${payment.VNPay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${payment.VNPay.secretKey}")
    private String vnp_SecretKey;

    @Value("${payment.VNPay.version}")
    private String vnp_Version;

    @Value("${payment.VNPay.command}")
    private String vnp_Command;

    @Value("${payment.VNPay.orderType}")
    private String vnp_OrderType;

    private final RestTemplate restTemplate;

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public CreatePaymentResult createPayment(Payment payment) {
        String url = buildPaymentUrl(
                payment.getId(),
                payment.getAmount(),
                "Order #" + payment.getOrderId(),
                payment.getCreatedAt());
        return new CreatePaymentResult(payment.getId(), url);
    }

    @Override
    public PaymentDto processCallback(Long paymentId, Map<String, String> params) {
        return queryStatus(paymentId, params.get("vnp_TransactionNo"));
    }

    @Override
    public PaymentDto processWebhook(Long paymentId, Map<String, String> params) {
        throw new UnsupportedOperationException(Message.Payment.WEBHOOK_NOT_SUPPORTED);
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;
        // VNPay spec: hash mọi field vnp_*, trừ vnp_SecureHash/vnp_SecureHashType.
        // Không mutate map đầu vào.
        Map<String, String> data = new HashMap<>(params);
        data.remove("vnp_SecureHash");
        data.remove("vnp_SecureHashType");
        data.entrySet().removeIf(e -> !e.getKey().startsWith("vnp_"));
        String hashData = buildHashData(data);
        String computedHash = hmacSHA512(vnp_SecretKey, hashData);
        return receivedHash.equalsIgnoreCase(computedHash);
    }

    @Override
    public PaymentDto queryStatus(Long paymentId, String transactionRef) {
        VnpayQueryResult result = queryTransaction(transactionRef, paymentId);
        PaymentResultStatus status = result.success()
                ? PaymentResultStatus.COMPLETED
                : ("UNKNOWN".equals(result.responseCode()) || "NO_RESPONSE".equals(result.responseCode())
                        ? PaymentResultStatus.UNKNOWN
                        : PaymentResultStatus.FAILED);
        return new PaymentDto(
                paymentId,
                null,
                PaymentMethod.VNPAY,
                null,
                status,
                result.transactionNo(),
                null);
    }

    public String buildPaymentUrl(Long paymentId, BigDecimal amount, String orderInfo, Instant createdAt) {
        Map<String, String> vnpParamsMap = new HashMap<>();
        vnpParamsMap.put("vnp_Version", this.vnp_Version);
        vnpParamsMap.put("vnp_Command", this.vnp_Command);
        vnpParamsMap.put("vnp_TmnCode", this.vnp_TmnCode);
        vnpParamsMap.put("vnp_CurrCode", "VND");
        vnpParamsMap.put("vnp_TxnRef", String.valueOf(paymentId));
        vnpParamsMap.put("vnp_OrderInfo", orderInfo);
        vnpParamsMap.put("vnp_OrderType", this.vnp_OrderType);
        vnpParamsMap.put("vnp_Locale", "vn");
        vnpParamsMap.put("vnp_ReturnUrl", this.vnp_ReturnUrl);
        vnpParamsMap.put("vnp_Amount", amount.multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        vnpParamsMap.put("vnp_IpAddr", "127.0.0.1");

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        vnpParamsMap.put("vnp_CreateDate", formatter.format(calendar.getTime()));

        long diffMinutes = ChronoUnit.MINUTES.between(createdAt, Instant.now());
        calendar.add(Calendar.MINUTE, Math.toIntExact(Math.max(15, 120 - diffMinutes)));
        vnpParamsMap.put("vnp_ExpireDate", formatter.format(calendar.getTime()));

        String hashData = buildHashData(vnpParamsMap);
        String secureHash = hmacSHA512(vnp_SecretKey, hashData);
        vnpParamsMap.put("vnp_SecureHash", secureHash);

        String queryString = getPaymentURL(vnpParamsMap, true);
        return vnp_PayUrl + "?" + queryString;
    }


    public VnpayQueryResult queryTransaction(String transactionRef, Long paymentId) {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnp_Version);
        params.put("vnp_Command", "querydr");
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_TxnRef", String.valueOf(paymentId));
        params.put("vnp_OrderInfo", "Query payment " + paymentId);
        params.put("vnp_IpAddr", "127.0.0.1");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String now = formatter.format(Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")).getTime());
        params.put("vnp_TransactionDate", now);
        params.put("vnp_CreateDate", now);

        String hashData = buildHashData(params);
        params.put("vnp_SecureHash", hmacSHA512(vnp_SecretKey, hashData));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
            params.forEach(formParams::add);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formParams, headers);
            Map<String, Object> response = restTemplate.postForObject(vnp_QueryUrl, request, Map.class);
            if (response == null) return new VnpayQueryResult(false, null, "NO_RESPONSE");


            String responseCode = String.valueOf(response.get("vnp_ResponseCode"));
            String transactionNo = String.valueOf(response.get("vnp_TransactionNo"));
            boolean success = "00".equals(responseCode);

            return new VnpayQueryResult(success, transactionNo, responseCode);
        } catch (Exception e) {
            return new VnpayQueryResult(false, null, "UNKNOWN");
        }
    }

    private String buildHashData(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException(Message.Common.NULL_PARAM_FOR_HMAC);
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            hmac512.init(new SecretKeySpec(key.getBytes(), "HmacSHA512"));
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public static String getPaymentURL(Map<String, String> paramsMap, boolean encodeKey) {
        return paramsMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    String key = encodeKey
                            ? URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII)
                            : entry.getKey();
                    String value = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII);
                    return key + "=" + value;
                }).collect(Collectors.joining("&"));
    }

}
