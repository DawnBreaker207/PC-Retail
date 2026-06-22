package com.dawn.common.security;


import com.dawn.common.security.handler.AuthEntryPointJwt;
import com.dawn.common.security.handler.RoleAccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
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
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/verify-otp",
            "/api/v1/auth/resend-otp"
    };

    private final AuthEntryPointJwt unauthorizedHandler;

    private final RoleAccessHandler roleAccessHandler;

    private final CorsConfig corsConfig;

    @Autowired(required = false)
    private final LogoutHandler logoutHandler;

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
