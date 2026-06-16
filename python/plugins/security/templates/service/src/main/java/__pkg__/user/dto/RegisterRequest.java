package {{basePackage}}.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "注册请求")
public class RegisterRequest {

    @NotBlank(message = "{auth.username.notblank}")
    @Size(min = 2, max = 64, message = "{auth.username.size}")
    @Schema(description = "用户名", example = "newuser")
    private String username;

    @NotBlank(message = "{auth.password.notblank}")
    @Size(min = 6, max = 128, message = "{auth.password.size}")
    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "邮箱", example = "newuser@example.com")
    private String email;
}
