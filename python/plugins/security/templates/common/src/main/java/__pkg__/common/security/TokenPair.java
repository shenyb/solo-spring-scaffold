package {{basePackage}}.common.security;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * JWT Token 对
 */
@Data
public class TokenPair {

    /** 访问令牌 */
    private String accessToken;

    /** 刷新令牌 */
    private String refreshToken;

    /** 访问令牌过期时间 */
    private LocalDateTime expiresAt;

    public TokenPair(String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }
}
