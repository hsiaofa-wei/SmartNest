package com.rental.controller;

import com.alipay.api.AlipayApiException;
import com.rental.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@CrossOrigin
@PreAuthorize("hasAnyRole('TENANT', 'ADMIN')")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付宝支付订单（电脑网站支付）
     */
    @PostMapping("/{orderId}/alipay")
    public ResponseEntity<Map<String, Object>> createAlipayOrder(@PathVariable Long orderId) {
        // 参数验证
        if (orderId == null || orderId <= 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "无效的订单ID");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            String form = paymentService.createAlipayOrder(orderId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "创建支付订单成功");
            response.put("form", form);
            return ResponseEntity.ok(response);
        } catch (AlipayApiException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "支付宝支付失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "创建支付订单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 创建支付宝二维码支付（当面付）
     */
    @PostMapping("/{orderId}/alipay/qr")
    public ResponseEntity<Map<String, Object>> createAlipayQrCode(@PathVariable Long orderId) {
        // 参数验证
        if (orderId == null || orderId <= 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "无效的订单ID");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            String qrCodeUrl = paymentService.createAlipayQrCode(orderId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "生成支付二维码成功");
            response.put("qrCodeUrl", qrCodeUrl);
            return ResponseEntity.ok(response);
        } catch (AlipayApiException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "支付宝二维码支付失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "生成支付二维码失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 支付宝异步回调接口
     */
    @PostMapping("/alipay/notify")
    @PreAuthorize("permitAll()")
    public String alipayNotify(@RequestParam Map<String, String> params) {
        try {
            boolean success = paymentService.handleAlipayCallback(params);
            return success ? "success" : "failure";
        } catch (Exception e) {
            // 记录日志
            return "failure";
        }
    }

    /**
     * 支付宝同步回调接口
     */
    @GetMapping("/alipay/return")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> alipayReturn(@RequestParam Map<String, String> params) {
        try {
            boolean success = paymentService.handleAlipayCallback(params);
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("code", 200);
                response.put("message", "支付成功");
                response.put("orderNo", params.get("out_trade_no"));
            } else {
                response.put("code", 500);
                response.put("message", "支付失败");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "处理支付结果失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 主动查询并更新订单支付状态
     */
    @GetMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> queryOrderStatus(@PathVariable Long orderId) {
        // 参数验证
        if (orderId == null || orderId <= 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "无效的订单ID");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            boolean success = paymentService.queryAndUpdateOrderStatus(orderId);
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("code", 200);
                response.put("message", "订单已支付");
                response.put("paymentStatus", "PAID");
            } else {
                response.put("code", 200);
                response.put("message", "订单未支付");
                response.put("paymentStatus", "UNPAID");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "查询订单状态失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}