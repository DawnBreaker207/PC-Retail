package com.dawn.common.security;


import com.dawn.common.security.handler.AuthEntryPointJwt;
import com.dawn.common.security.handler.RoleAccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String[] SWAGGER_URL = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/api/v1/v3/api-docs/**",
            "/api/v1/v3/api-docs",
            "/api/v1/api-docs/**",
            "/api/v1/api-docs"};


    private static final String[] PUBLIC_URL = {
            "/api/v1/auth/**",
            "/api/v1/brands",
            "/api/v1/categories",
            "/api/v1/skus",
            "/api/v1/skus/**",
            "/api/v1/builder/**",
            "/api/v1/articles/**",
            "/api/v1/articles",
            // gợi ý cấu hình theo sở thích/ngân sách (chỉ đọc build_template)
            "/api/v1/ai/build-suggestions",
            "/api/v1/promotion/**",
            "/api/v1/cart/guest/**",
            "/api/v1/orders/guest-checkout",
            "/api/v1/payments/callback/**",
            "/api/v1/payments/webhook/**",
            // service-to-service; InternalTokenFilter chặn bằng shared token
            "/api/v1/internal/**",
            // STOMP handshake must be reachable; authentication happens on
            // the CONNECT frame (see StompAuthChannelInterceptor).
            "/ws/**",
            "/actuator/**"
    };

    private final AuthEntryPointJwt unauthorizedHandler;

    private final RoleAccessHandler roleAccessHandler;

    private final CorsConfig corsConfig;

    private final AuthTokenFilter authTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfig.config()))
                .csrf(CsrfConfigurer::disable)
                .exceptionHandling(this::configExceptionHandling)
                .sessionManagement(this::configSession)
                .authorizeHttpRequests(this::configAuth);

        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void configExceptionHandling(ExceptionHandlingConfigurer<HttpSecurity> config) {
        config
                .authenticationEntryPoint(unauthorizedHandler)
                .accessDeniedHandler(roleAccessHandler);
    }

    private void configSession(SessionManagementConfigurer<HttpSecurity> config) {
        config
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }


    private void configAuth(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry config) {
        config.requestMatchers(SWAGGER_URL)
                .permitAll()
                .requestMatchers(PUBLIC_URL)
                .permitAll()
                .anyRequest()
                .authenticated();
    }
}
