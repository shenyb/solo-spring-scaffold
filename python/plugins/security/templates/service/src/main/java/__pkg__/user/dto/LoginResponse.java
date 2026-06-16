package {{basePackage}}.user.dto;

import {{basePackage}}.common.security.TokenPair;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "过期时间（秒）", example = "3600")
    private Long expiresIn;

    public static LoginResponse from(TokenPair tokenPair, long expiresInSec) {
        LoginResponse resp = new LoginResponse();
        resp.setAccessToken(tokenPair.getAccessToken());
        resp.setRefreshToken(tokenPair.getRefreshToken());
        resp.setExpiresIn(expiresInSec);
        return resp;
    }
}
