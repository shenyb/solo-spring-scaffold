package {{basePackage}}.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT 工具类
 * <p>
 * 负责令牌的生成、解析、验证。
 * 使用 HMAC-SHA256 签名。
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
            @Value("${jwt.secret:default-secret-key-must-be-at-least-256-bits-long-for-hs256}") String secret,
            @Value("${jwt.access-token-validity:3600000}") long accessTokenValidityMs,
            @Value("${jwt.refresh-token-validity:86400000}") long refreshTokenValidityMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    /**
     * 生成 Token 对（access + refresh）
     */
    public TokenPair generateTokenPair(Long userId, String username) {
        LocalDateTime now = LocalDateTime.now();
        Date accessExpiry = toDate(now.plusMinutes(accessTokenValidityMs / 60000));
        Date refreshExpiry = toDate(now.plusMinutes(refreshTokenValidityMs / 60000));

        String accessToken = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(toDate(now))
                .expiration(accessExpiry)
                .signWith(key)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(toDate(now))
                .expiration(refreshExpiry)
                .signWith(key)
                .compact();

        return new TokenPair(accessToken, refreshToken, now.plusMinutes(accessTokenValidityMs / 60000));
    }

    /**
     * 从令牌中解析 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证令牌是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.debug("不支持的 JWT: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("JWT 格式错误: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT 为空: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 从令牌获取用户 ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    private Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }
}
