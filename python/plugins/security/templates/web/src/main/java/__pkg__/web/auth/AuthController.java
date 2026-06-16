package {{basePackage}}.web.auth;

import {{basePackage}}.common.api.Result;
import {{basePackage}}.common.security.TokenPair;
import {{basePackage}}.user.dto.LoginRequest;
import {{basePackage}}.user.dto.LoginResponse;
import {{basePackage}}.user.dto.RegisterRequest;
import {{basePackage}}.user.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "登录、注册、刷新令牌")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.access-token-validity:3600000}")
    private long accessTokenValidityMs;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenPair tokenPair = authService.login(request);
        return Result.success(LoginResponse.from(tokenPair, accessTokenValidityMs / 1000));
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestParam String refreshToken) {
        TokenPair tokenPair = authService.refresh(refreshToken);
        return Result.success(LoginResponse.from(tokenPair, accessTokenValidityMs / 1000));
    }
}
