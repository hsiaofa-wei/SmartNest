package com.rental.util;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.alipay.api.internal.util.AlipaySignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AlipayUtil {

    @Value("${alipay.gateway-url}")
    private String gatewayUrl;

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.app-private-key}")
    private String appPrivateKey;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;

    private static final String FORMAT = "JSON";
    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYPE = "RSA2";
    
    // 支付宝金额限制
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");
    
    /**
     * 验证金额是否符合支付宝要求
     */
    private void validateAmount(BigDecimal amount) throws AlipayApiException {
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new AlipayApiException("金额不能小于0.01元");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new AlipayApiException("沙箱环境单笔金额不能超过10000元");
        }
        // 确保金额是两位小数
        if (amount.scale() != 2) {
            throw new AlipayApiException("金额必须保留两位小数");
        }
    }

    /**
     * 创建支付宝客户端
     */
    public AlipayClient createAlipayClient() {
        return new DefaultAlipayClient(
                gatewayUrl, appId, appPrivateKey, FORMAT, CHARSET, alipayPublicKey, SIGN_TYPE);
    }

    /**
     * 创建电脑网站支付表单
     */
    public String createPagePayForm(String outTradeNo, String subject, String totalAmount, String body, Long orderId) throws AlipayApiException {
        // 验证金额
        BigDecimal amount = new BigDecimal(totalAmount);
        validateAmount(amount);
        
        AlipayClient alipayClient = createAlipayClient();
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        // 将订单ID附加到returnUrl中，确保支付成功后前端能正确加载订单
        request.setReturnUrl(returnUrl + orderId);

        StringBuilder bizContent = new StringBuilder();
        bizContent.append("{");
        bizContent.append("\"out_trade_no\":\"").append(outTradeNo).append("\",");
        bizContent.append("\"product_code\":\"FAST_INSTANT_TRADE_PAY\",");
        bizContent.append("\"total_amount\":\"").append(totalAmount).append("\",");
        bizContent.append("\"subject\":\"").append(subject).append("\",");
        bizContent.append("\"body\":\"").append(body).append("\"");
        bizContent.append("}");

        request.setBizContent(bizContent.toString());
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);

        if (response.isSuccess()) {
            return response.getBody();
        } else {
            throw new AlipayApiException("创建支付表单失败：" + response.getMsg());
        }
    }

    /**
     * 验证支付宝回调签名
     */
    public boolean verifyCallback(Map<String, String> params) throws AlipayApiException {
        return AlipaySignature.rsaCheckV2(params, alipayPublicKey, CHARSET, SIGN_TYPE);
    }

    /**
     * 退款
     */
    public boolean refund(String outTradeNo, String tradeNo, String refundAmount, String refundReason, String outRequestNo) throws AlipayApiException {
        // 验证退款金额
        validateAmount(new BigDecimal(refundAmount));
        
        AlipayClient alipayClient = createAlipayClient();
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        // 构建退款请求参数
        StringBuilder bizContent = new StringBuilder();
        bizContent.append("{");
        bizContent.append("\"out_trade_no\":\"").append(outTradeNo).append("\",");
        
        // 可选参数：如果有支付宝交易号，优先使用
        if (tradeNo != null && !tradeNo.isEmpty()) {
            bizContent.append("\"trade_no\":\"").append(tradeNo).append("\",");
        }
        
        bizContent.append("\"refund_amount\":\"").append(refundAmount).append("\",");
        bizContent.append("\"refund_reason\":\"").append(refundReason).append("\",");
        
        // 退款请求号，用于部分退款和多次退款
        if (outRequestNo == null || outRequestNo.isEmpty()) {
            outRequestNo = "REFUND_" + System.currentTimeMillis();
        }
        bizContent.append("\"out_request_no\":\"").append(outRequestNo).append("\"");
        
        bizContent.append("}");
        request.setBizContent(bizContent.toString());
        
        // 执行退款请求
        AlipayTradeRefundResponse response = alipayClient.execute(request);
        
        // 详细记录退款结果
        if (response.isSuccess()) {
            System.out.println("退款成功：交易号=" + response.getTradeNo() + ", 退款金额=" + response.getRefundFee());
            return true;
        } else {
            // 退款失败，抛出详细错误信息
            throw new AlipayApiException("退款失败：" + response.getSubMsg() + " (错误码：" + response.getSubCode() + ")");
        }
    }
    
    /**
     * 退款查询
     */
    public boolean queryRefund(String outTradeNo, String outRequestNo) throws AlipayApiException {
        AlipayClient alipayClient = createAlipayClient();
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        
        StringBuilder bizContent = new StringBuilder();
        bizContent.append("{");
        bizContent.append("\"out_trade_no\":\"").append(outTradeNo).append("\",");
        bizContent.append("\"out_request_no\":\"").append(outRequestNo).append("\"");
        bizContent.append("}");
        
        request.setBizContent(bizContent.toString());
        AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);
        
        if (response.isSuccess()) {
            System.out.println("退款查询成功：退款状态=" + response.getRefundStatus());
            return "SUCCESS".equals(response.getRefundStatus());
        } else {
            throw new AlipayApiException("退款查询失败：" + response.getSubMsg() + " (错误码：" + response.getSubCode() + ")");
        }
    }

    /**
     * 生成支付宝当面付动态二维码
     */
    public String createQrCode(String outTradeNo, String subject, String totalAmount, String storeId) throws AlipayApiException {
        // 验证金额
        BigDecimal amount = new BigDecimal(totalAmount);
        validateAmount(amount);
        
        AlipayClient alipayClient = createAlipayClient();
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(notifyUrl);

        StringBuilder bizContent = new StringBuilder();
        bizContent.append("{");
        bizContent.append("\"out_trade_no\":\"").append(outTradeNo).append("\",");
        bizContent.append("\"product_code\":\"FACE_TO_FACE_PAYMENT\",");
        bizContent.append("\"subject\":\"").append(subject).append("\",");
        bizContent.append("\"total_amount\":\"").append(totalAmount).append("\",");
        bizContent.append("\"timeout_express\":\"30m\"");
        // 可选参数，只有当storeId有效时才添加
        if (storeId != null && !storeId.isEmpty()) {
            bizContent.append(",\"store_id\":\"").append(storeId).append("\"");
        }
        bizContent.append("}");
        request.setBizContent(bizContent.toString());

        AlipayTradePrecreateResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            // 返回二维码图片的URL
            return response.getQrCode();
        } else {
            // 改进错误处理，返回更详细的错误信息
            String errorMsg = "生成二维码失败：" + response.getMsg();
            if (response.getSubCode() != null) {
                errorMsg += ", 错误代码：" + response.getSubCode();
            }
            if (response.getSubMsg() != null) {
                errorMsg += ", 错误描述：" + response.getSubMsg();
            }
            throw new AlipayApiException(errorMsg);
        }
    }
    
    /**
     * 查询支付宝订单状态
     */
    public String queryOrderStatus(String outTradeNo) throws AlipayApiException {
        AlipayClient alipayClient = createAlipayClient();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        
        StringBuilder bizContent = new StringBuilder();
        bizContent.append("{");
        bizContent.append("\"out_trade_no\":\"").append(outTradeNo).append("\"");
        bizContent.append("}");
        request.setBizContent(bizContent.toString());
        
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            // 返回支付宝交易状态
            return response.getTradeStatus();
        } else {
            // 改进错误处理，返回更详细的错误信息
            String errorMsg = "查询订单状态失败：" + response.getMsg();
            if (response.getSubCode() != null) {
                errorMsg += ", 错误代码：" + response.getSubCode();
            }
            if (response.getSubMsg() != null) {
                errorMsg += ", 错误描述：" + response.getSubMsg();
            }
            throw new AlipayApiException(errorMsg);
        }
    }
}