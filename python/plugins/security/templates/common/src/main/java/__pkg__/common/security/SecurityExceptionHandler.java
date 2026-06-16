package {{basePackage}}.common.security;

import {{basePackage}}.common.api.Result;
import {{basePackage}}.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 安全相关异常处理器
 * <p>
 * 处理 Spring Security 抛出的 403 Forbidden 异常
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class SecurityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.warn("权限不足: user={}, message={}", auth != null ? auth.getPrincipal() : "anonymous", e.getMessage());
        return Result.fail(ErrorCode.FORBIDDEN);
    }
}
