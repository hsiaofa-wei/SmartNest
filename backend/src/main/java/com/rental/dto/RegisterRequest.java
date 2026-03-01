package com.rental.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$", 
             message = "密码至少8位，包含大小写字母、数字和特殊字符")
    private String password;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String captcha;

    @NotBlank(message = "验证码key不能为空")
    private String captchaKey;

    @NotBlank(message = "角色不能为空")
    private String role; // LANDLORD or TENANT

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 20, message = "真实姓名长度必须在2-20个字符之间")
    private String realName;

    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$", 
             message = "身份证号格式不正确")
    private String idCard;

    @NotNull(message = "必须同意用户协议和隐私政策")
    private Boolean agreementAccepted;

    // 以下字段为可选，用于未来扩展
    private String verificationType; // PHONE or EMAIL

    private String verificationCode;

    private String verificationKey;
}

