package com.rental.controller;

import com.alipay.api.AlipayApiException;
import com.rental.service.PaymentService;
import com.rental.util.AlipayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝退款回调控制器
 */
@RestController
@RequestMapping("/api/alipay/refund")
public class RefundNotifyController {

    private static final Logger logger = LoggerFactory.getLogger(RefundNotifyController.class);

    @Autowired
    private AlipayUtil alipayUtil;

    @Autowired
    private PaymentService paymentService;

    /**
     * 处理支付宝退款回调通知
     * @param request 退款回调请求
     * @return 响应结果
     */
    @PostMapping("/notify")
    public String handleRefundNotify(HttpServletRequest request) {
        try {
            // 将请求参数转换为Map
            Map<String, String> params = convertRequestToMap(request);
            
            logger.info("收到支付宝退款回调，参数数量: {}, 参数: {}", params.size(), params);
            
            // 验证签名
            boolean signVerified = alipayUtil.verifyCallback(params);
            logger.info("支付宝退款回调签名验证结果: {}", signVerified);
            
            if (!signVerified) {
                logger.error("支付宝退款回调签名验证失败");
                return "failure";
            }
            
            // 获取回调关键参数
            String tradeNo = params.get("trade_no");
            String outTradeNo = params.get("out_trade_no");
            String refundStatus = params.get("refund_status");
            String outRequestNo = params.get("out_request_no");
            String refundAmount = params.get("refund_amount");
            
            logger.info("退款回调关键参数: trade_no={}, out_trade_no={}, refund_status={}, out_request_no={}, refund_amount={}",
                    tradeNo, outTradeNo, refundStatus, outRequestNo, refundAmount);
            
            // 处理退款状态
            if ("REFUND_SUCCESS".equals(refundStatus)) {
                // 退款成功，更新订单状态
                logger.info("退款成功，更新订单状态");
                // 这里可以添加退款成功后的业务逻辑
                // 例如：记录退款日志、发送通知等
            } else {
                logger.error("退款失败，状态: {}", refundStatus);
            }
            
            // 返回成功响应
            return "success";
            
        } catch (AlipayApiException e) {
            logger.error("支付宝退款回调处理异常: {}", e.getMessage(), e);
            return "failure";
        } catch (Exception e) {
            logger.error("退款回调处理异常: {}", e.getMessage(), e);
            return "failure";
        }
    }
    
    /**
     * 将HttpServletRequest参数转换为Map
     */
    private Map<String, String> convertRequestToMap(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            String value = request.getParameter(name);
            params.put(name, value);
        }
        return params;
    }
}