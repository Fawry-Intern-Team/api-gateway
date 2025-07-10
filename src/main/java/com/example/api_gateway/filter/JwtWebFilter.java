package com.example.api_gateway.filter;

import com.example.api_gateway.service.JwtService;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtWebFilter implements WebFilter {

    private final JwtService jwtService;
    private final WebClient.Builder webClientBuilder;

    public JwtWebFilter(JwtService jwtService, WebClient.Builder webClientBuilder) {
        this.jwtService = jwtService;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);
        String email;
        System.out.println(token);
        try {
            email = jwtService.extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return chain.filter(exchange);
        }

        Mono<String> validTokenMono;

        if (!jwtService.validateToken(token)) {
            String refreshToken = request.getHeaders().getFirst("Refresh-Token");

            if (refreshToken != null && jwtService.validateToken(refreshToken)) {
                String url = "http://auth-service/refresh";
                validTokenMono = webClientBuilder.build()
                        .get()
                        .uri(url)
                        .header("Refresh-Token", refreshToken)
                        .retrieve()
                        .bodyToMono(String.class);
            } else {
                return Mono.error(new BadCredentialsException("Invalid token"));
            }
        } else {
            validTokenMono = Mono.just(token);
        }

        return validTokenMono.flatMap(validToken -> {
            List<String> roles = jwtService.extractClaim(validToken,
                    claims -> claims.get("roles", List.class));


            List<SimpleGrantedAuthority> authorities = null;
            try {
                authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
            SecurityContext context = new SecurityContextImpl(auth);
            System.out.println(context.getAuthentication());
            System.out.println("Principal: " + exchange.getPrincipal());
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
        });
    }
}