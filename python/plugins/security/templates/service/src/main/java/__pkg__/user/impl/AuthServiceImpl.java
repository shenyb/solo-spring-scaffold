package {{basePackage}}.user.impl;

import {{basePackage}}.common.exception.BizException;
import {{basePackage}}.common.exception.ErrorCode;
import {{basePackage}}.common.security.JwtTokenProvider;
import {{basePackage}}.common.security.TokenPair;
import {{basePackage}}.user.AuthService;
import {{basePackage}}.user.User;
import {{basePackage}}.user.UserMapper;
import {{basePackage}}.user.dto.LoginRequest;
import {{basePackage}}.user.dto.RegisterRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public TokenPair login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BizException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }

        log.info("用户登录成功: username={}", user.getUsername());
        return jwtTokenProvider.generateTokenPair(user.getId(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BizException(ErrorCode.DATA_EXISTS, "用户名");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setStatus(1);
        userMapper.insert(user);

        log.info("用户注册成功: id={}, username={}", user.getId(), user.getUsername());
        return user.getId();
    }

    @Override
    public TokenPair refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "刷新令牌无效或已过期");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "用户");
        }

        return jwtTokenProvider.generateTokenPair(user.getId(), user.getUsername());
    }
}
