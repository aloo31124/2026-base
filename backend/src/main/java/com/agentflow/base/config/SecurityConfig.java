package com.agentflow.base.config;

import com.agentflow.base.model.dto.ApiResponse;
import com.agentflow.base.security.JwtAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties({LineOAuthProperties.class, MailProperties.class})
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception { return configuration.getAuthenticationManager(); }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter, ObjectMapper mapper) throws Exception {
        return http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((req, res, ex) -> writeError(res, mapper, HttpStatus.UNAUTHORIZED, "尚未登入或 JWT 已失效。"))
                .accessDeniedHandler((req, res, ex) -> writeError(
                    res,
                    mapper,
                    HttpStatus.FORBIDDEN,
                    accessDeniedMessage(req.getRequestURI())
                )))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build();
    }

    private void writeError(jakarta.servlet.http.HttpServletResponse response, ObjectMapper mapper, HttpStatus status, String message) throws java.io.IOException {
        response.setStatus(status.value()); response.setContentType(MediaType.APPLICATION_JSON_VALUE); response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }

    /**
     * 依管理 API 路徑提供可追溯的模組權限錯誤訊息。
     */
    private String accessDeniedMessage(String requestUri) {
        if (requestUri.startsWith("/api/admin/system-reports")) {
            return "[系統報表] [api] 無系統管理員權限。";
        }
        if (requestUri.startsWith("/api/admin/company-supervisor-management")) {
            return "[公司主管管理] [api] 無系統管理員權限。";
        }
        if (requestUri.startsWith("/api/task-assignment")) {
            return "[任務指派] [api] 無主管權限。";
        }
        if (requestUri.startsWith("/api/admin/registration-management")) {
            return "[註冊登入管理] [api] 無系統管理員權限。";
        }
        return "[使用者角色] [api] 無系統管理員權限。";
    }

    @Bean CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origin}") String origin) {
        CorsConfiguration config = new CorsConfiguration();
        // 支援逗號分隔多來源：Cloud Run 同一服務有雜湊型與確定型兩種網址，需同時放行
        config.setAllowedOrigins(java.util.Arrays.stream(origin.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**", config); return source;
    }
}
