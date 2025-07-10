package com.example.api_gateway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class ApiVersionFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String version = getApiVersion(exchange);
        
        // Add version info to request headers for downstream services
        exchange.getRequest().mutate()
                .header("X-API-Version", version)
                .build();
        
        // Log version info
        System.out.println("API Version: " + version + " for path: " + path);
        
        return chain.filter(exchange);

    }

    /**
     * Extract API version from:
     * 1. Path-based versioning (/v1/, /v2/)
     * 2. Header-based versioning (X-API-Version header)
     * 3. Default to v1 if no version specified
     */
    private String getApiVersion(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        
        // Check for path-based versioning
        if (path.startsWith("/v1/")) {
            return "v1";
        } else if (path.startsWith("/v2/")) {
            return "v2";
        }
        
        // Check for header-based versioning
        String versionHeader = exchange.getRequest().getHeaders().getFirst("X-API-Version");
        if (versionHeader != null && !versionHeader.isEmpty()) {
            return versionHeader;
        }
        
        // Default version
        return "v1";
    }
} 