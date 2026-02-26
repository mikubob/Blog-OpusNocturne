package com.xuan.entity.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * 3.5修改密码 DTO
 * 
 * @author 玄〤
 */
@Data
@Schema(description = "修改密码请求参数")
public class ChangePasswordDTO implements Serializable {

    @Schema(description = "旧密码")
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码")
    @NotBlank(message = "新密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String newPassword;

    @Schema(description = "确认新密码")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
