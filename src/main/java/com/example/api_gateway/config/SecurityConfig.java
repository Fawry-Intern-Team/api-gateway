package com.example.api_gateway.config;

import com.example.api_gateway.filter.ApiVersionFilter;
import com.example.api_gateway.filter.JwtWebFilter;
import com.example.api_gateway.filter.RoleBasedAccessFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private JwtWebFilter jwtWebFilter;

    @Autowired
    private RoleBasedAccessFilter roleBasedAccessFilter;

    @Autowired
    private ApiVersionFilter apiVersionFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(authz -> authz
                        .pathMatchers("/api/auth/login").permitAll()
                        .pathMatchers("/api/auth/register").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAfter(roleBasedAccessFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterBefore(apiVersionFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}

