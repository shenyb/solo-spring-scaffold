package {{basePackage}}.user;

import {{basePackage}}.common.security.TokenPair;
import {{basePackage}}.user.dto.LoginRequest;
import {{basePackage}}.user.dto.RegisterRequest;

public interface AuthService {

    /**
     * 用户登录
     */
    TokenPair login(LoginRequest request);

    /**
     * 用户注册
     */
    Long register(RegisterRequest request);

    /**
     * 刷新令牌
     */
    TokenPair refresh(String refreshToken);
}
