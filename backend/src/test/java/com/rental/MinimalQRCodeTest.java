package com.rental;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;

public class MinimalQRCodeTest {

    public static void main(String[] args) {
        // 直接使用application.yml中的配置
        String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
        String appId = "9021000158676064";
        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDE3fcxQmMNSNz20kZvBze0pcYCjh/1czkHwPwDrembCGRN19Etwnb3hMuuk64U82oPFH4gb3OD08EXWCBIx+K0Y7kOnfsQYNegKAaoG2/s7miU53+ajeofOCtjY6Q3s5NSuOYx6AQ4mxOFQ+Es8CMe1H6WOe++eRYib+4mojIvBVwC3MG2w97Paha9sa7R4r/QCDVisw0w4eS1zZq1dt7Y4B8W3Hp6xBU+OfsvvfUcWDU/3mbaDGJw+HT6S2AYUUWqkgSbLyWyfxpEEgU7jzppbIW+UiCgleo4cggRtgk6lh8STTnJ0bs4ot0YdotfT0OPk2+Q9+MvgwehKbbQqZnFAgMBAAECggEATa2TcsFSGhTM8yrnFboQVdwvNCTmIVA0iYWxKrZ9fWmjQj/MzrRzgpjIUXr97lBZYdfhKGUxfCuafd6M5orQPgM3KrhVLxI3In+ImN4ynJc5dr+dTtt+LJFaNV5ElqHpmHNSiRCWUcuGuUeeWr/p66lGxXnYyhw3CqGFo4LOMFTHZ/6W2TVG88xDg3+0HMTXp4z+CJhqe+O634QOj0m5YkIDw7hQEsvtEkWfP9R5Ex0AP9RU9quOhbdRrLgxDLSvnp0pEAzm3PA7gfcpNqa9bVzZ5mbBZqAwyjW08sR1IO+ahES5Ic/GEaRa94ReHxjq5+hri9m3ZZgTzAhgLwzuBQKBgQD/SeAoU0qDOkFAokoZiD06rvCxgyu9q27hS8eDpIycmt54jf5yJcKxXzhL+u2PdzRhFd9fkJBiCtG/VJPGl1tIiESALNqEgg/l5mAsvGLLMi6cz9HFXOpMHCajG7vgfvPymgM+RDM/qcfiiSAy96gryriMrv05zhyB9J9k+pk49wKBgQDFamlmZYCVQiWvZCBv1PestHbiaSvnlG99yJ4SXDhpTqJdRL7o1GWHQE4lRvkdtP+uTIBVFHm5AjGvQvUrRSD9kQ30IPk8M9bIPOtr4Khjux/wwoojtmng6SMZt6TNv9+O4q4invvAD5GlBELRB6TGuH8t9eAO7hKRUKsGz5uwIwKBgEOtdA18tz4+zVmzLG3EXmcWRZzpp0jrD8QOcOAHWXeS3vOawR/Fvu2Czsvlw+DKu0oZoFQXYQ98ysosQ9jb+0kpRu+r9Go4iLpr7octkgtITG8p8PSfg0WWGPAYxB/pyAZCqiVxsxxniCQYMj8QxLaHUZGKOnt+lrQlA7SjcsdVAoGBAJibqBrWAE5hsgwhlMmekk/ZucVQk/YTUxSjTz7WN22Ashbzm7kU599kIITElrMWRkiRLuuWRnp0bcAUPL5UVCpwRZ8hFkopsLFeFodwRfvCnfBaEruEudh5oP6ybh33Y3RrWz28NNTFtEsiPhykkgZkfTwkRC6Gb6gR+gPr3WiNAoGAHtL9zrQrWsm62U1NUde3IAsvuWINawUyofOWL7QbcjCbK35si9hP6sbMRkyDC9GanBI0ZHOuzCPTfHFvon4YAjFhhAiXbLs+DdmXmc9JZ68HSFAcEFZ0xIs1CWsuR8dkbYRCGAsc4TrN3VsuUXt6Ck6Yyrf/2LJOzV6NTgRaf/Q=";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3rCtOTY6ptGWWLB9VeTl/eCFStYArBfUgephu8B4n7k3JEYWgraIZ3uhG4Wh5TjJzJjsO53b/J9GydKC/sgAh8BL+Bp6Fdsg+Tnh5J2xSy0C3qLmAahKCxPhwg5y/VfdUeXCjxvq4oxDUSdU7D5VQg01bDZJxtb7WdEGXWkPWTHG3/2wiCMfllJxZKuPtAolSXq6LnPXIGh3zBNC9rQPEMHMCP8grbO89+Njl+KR/LcM/2zJ24THLqPsi9jc8VipcXi//miQZrAOrdraTV7SZw2QBNL1DtDKEhzOqGo6iZCM9nF4TnZXvXEE1b7PWNEU19RvpSgdqi69M/Ho8pdRAwIDAQAB";

        final String FORMAT = "JSON";
        final String CHARSET = "UTF-8";
        final String SIGN_TYPE = "RSA2";

        try {
            System.out.println("=== 最小化二维码生成测试 ===");
            
            // 1. 初始化客户端（使用最简配置）
            AlipayClient alipayClient = new DefaultAlipayClient(
                gatewayUrl,
                appId,
                appPrivateKey,
                FORMAT,
                CHARSET,
                alipayPublicKey,
                SIGN_TYPE
            );
            
            System.out.println("客户端初始化成功");
            System.out.println("网关URL: " + gatewayUrl);
            System.out.println("APPID: " + appId);
            System.out.println("私钥格式: " + (appPrivateKey.contains("BEGIN") ? "包含BEGIN标记" : "不包含BEGIN标记"));
            System.out.println("公钥格式: " + (alipayPublicKey.contains("BEGIN") ? "包含BEGIN标记" : "不包含BEGIN标记"));
            
            // 2. 创建最简单请求
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            
            // 3. 使用绝对最小参数
            String bizContent = "{" +
                "\"out_trade_no\":\"TEST" + System.currentTimeMillis() + "\"," +
                "\"total_amount\":\"0.01\"," +
                "\"subject\":\"test\"" +
                "}";
            
            System.out.println("业务参数: " + bizContent);
            request.setBizContent(bizContent);
            
            // 4. 执行请求并打印详细结果
            System.out.println("执行请求...");
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            
            System.out.println("=== 响应详情 ===");
            System.out.println("Code: " + response.getCode());
            System.out.println("Msg: " + response.getMsg());  
            System.out.println("SubCode: " + response.getSubCode());
            System.out.println("SubMsg: " + response.getSubMsg());
            System.out.println("Body: " + response.getBody());
            
            if (response.isSuccess()) {
                System.out.println("✅ 二维码生成成功!");
                System.out.println("二维码内容: " + response.getQrCode());
            } else {
                System.out.println("❌ 二维码生成失败!");
                analyzeError(response);
            }
            
        } catch (Exception e) {
            System.out.println("💥 异常信息: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void analyzeError(AlipayTradePrecreateResponse response) {
        String subCode = response.getSubCode();
        String subMsg = response.getSubMsg();
        
        System.out.println("=== 错误分析 ===");
        
        if (subCode != null) {
            switch (subCode) {
                case "isv.invalid-signature":
                    System.out.println("🔑 问题: 签名验证失败");
                    System.out.println("💡 解决方案: 检查支付宝公钥是否正确，是否为支付宝提供的公钥而非应用公钥");
                    break;
                case "isv.invalid-app-id":
                    System.out.println("🔑 问题: APPID无效");
                    System.out.println("💡 解决方案: 检查APPID配置是否正确");
                    break;
                case "isv.missing-method":
                    System.out.println("📋 问题: 参数缺失");
                    System.out.println("💡 解决方案: 检查bizContent格式是否正确");
                    break;
                default:
                    System.out.println("❓ 问题: " + subCode);
                    System.out.println("📝 描述: " + subMsg);
                    System.out.println("💡 解决方案: 请根据错误信息联系支付宝技术支持或检查配置");
            }
        } else {
            System.out.println("❓ 未知错误: " + response.getMsg());
            System.out.println("💡 解决方案: 检查网络连接、网关URL是否正确");
        }
    }
}
