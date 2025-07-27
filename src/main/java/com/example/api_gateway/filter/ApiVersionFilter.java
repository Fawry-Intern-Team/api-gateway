package com.example.api_gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class ApiVersionFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        System.out.println("ApiVersionFilter");

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        String version = getApiVersion(request);

        // Add version info to response headers
        response.getHeaders().add("X-API-Version", version);

        // Log version info
        System.out.println("API Version: " + version + " for path: " + path);

        return chain.filter(exchange);
    }

    private String getApiVersion(ServerHttpRequest request) {
        String path = request.getURI().getPath();

        if (path.startsWith("/v1/")) {
            return "v1";
        } else if (path.startsWith("/v2/")) {
            return "v2";
        }

        String versionHeader = request.getHeaders().getFirst("X-API-Version");
        return (versionHeader != null && !versionHeader.isEmpty()) ? versionHeader : "v1";
    }
}
