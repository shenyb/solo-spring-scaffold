package {{basePackage}}.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security 配置
 * <p>
 * 默认规则：
 * - 无状态 JWT 会话
 * - /api/auth/** 公开（登录/注册）
 * - /swagger-ui/**, /api-docs/** 公开（文档）
 * - /h2-console/** 公开（开发期 H2 控制台）
 * - 其余接口需要认证
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 无状态会话
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 关闭 CSRF（JWT 场景不需要）
            .csrf(AbstractHttpConfigurer::disable)
            // 关闭 formLogin / httpBasic
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            // 异常处理
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
            // H2 console 需要 sameOrigin iframe
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // 路由权限
            .authorizeHttpRequests(auth -> auth
                // 认证接口
                .requestMatchers("/api/auth/**").permitAll()
                // Swagger / OpenAPI
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/api-docs/**",
                    "/v3/api-docs/**"
                ).permitAll()
                // H2 控制台（仅开发期）
                .requestMatchers("/h2-console/**").permitAll()
                // Actuator 健康检查
                .requestMatchers("/actuator/health").permitAll()
                // 其余需要认证
                .anyRequest().authenticated())
            // JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
