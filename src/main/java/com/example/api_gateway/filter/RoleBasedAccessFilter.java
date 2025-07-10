package com.example.api_gateway.filter;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class RoleBasedAccessFilter implements WebFilter {

    private static final Map<String, List<String>> ROLE_ACCESS_MAP = Map.of(
            "/api/auth/protected", List.of("ADMIN"),
            "/api/products", List.of("USER", "ADMIN"),
            "/api/orders", List.of("USER", "ADMIN")
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        System.out.println("RoleBasedAccessFilter Triggered");
        String path = exchange.getRequest().getPath().value();

        return ROLE_ACCESS_MAP.entrySet().stream()
                .filter(entry -> path.startsWith(entry.getKey()))
                .findFirst()
                .map(entry -> checkRole(exchange, entry.getValue(), chain))
                .orElse(chain.filter(exchange));
    }

    private Mono<Void> checkRole(ServerWebExchange exchange, List<String> allowedRoles, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> {
                    System.out.println("Allowed roles: " + allowedRoles);
                    boolean match = auth.getAuthorities().stream().anyMatch(a -> {
                        String role = a.getAuthority().replace("ROLE_", "");
                        System.out.println("Comparing: " + role + " vs " + allowedRoles);
                        return allowedRoles.contains(role);
                    });
                    return match;
                })
                .flatMap(auth -> chain.filter(exchange))
                .switchIfEmpty(Mono.defer(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }));
    }
}