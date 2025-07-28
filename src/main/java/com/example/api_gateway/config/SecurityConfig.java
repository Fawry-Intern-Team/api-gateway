package com.example.api_gateway.config;

import com.example.api_gateway.filter.ApiVersionFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CookieTokenAuthenticationConverter cookieTokenAuthenticationConverter;


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Matcher for public paths
        ServerWebExchangeMatcher publicPaths = ServerWebExchangeMatchers.pathMatchers(
                "/auth/login", "/auth/register", "/auth/refresh", "/actuator/**",
                "/oauth2/**", "/login/**", "/favicon.ico"
        );

        // Negate to apply on all except public paths
        ServerWebExchangeMatcher securedPaths = new NegatedServerWebExchangeMatcher(publicPaths);

        return http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.addAllowedOrigin("http://localhost:4200");
                    config.addAllowedHeader("*");
                    config.addAllowedMethod("*");
                    config.setAllowCredentials(true);
                    return config;
                }))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/auth/login", "/auth/register", "/auth/refresh", "/actuator/**",
                                "/oauth2/**", "/login/**", "/favicon.ico").permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((ServerWebExchange exchange, AuthenticationException ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return Mono.empty();
                        })
                )
                .securityMatcher(securedPaths) // apply only to secured paths
                .oauth2ResourceServer(auth -> auth
                        .bearerTokenConverter(cookieTokenAuthenticationConverter)
                        .jwt(Customizer.withDefaults())
                )
                .build();

    }


    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
