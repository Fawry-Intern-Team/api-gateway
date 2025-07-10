package com.example.api_gateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Unexpected error";

        if (ex instanceof org.springframework.security.authentication.BadCredentialsException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Invalid credentials";
        } else if (ex instanceof NullPointerException) {
            status = HttpStatus.BAD_REQUEST;
            message = "Null value encountered";
        }

        String json = """
            {
              "timestamp": "%s",
              "status": %d,
              "error": "%s",
              "message": "%s",
              "path": "%s"
            }
            """.formatted(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath()
            ).replace("\n", "").replace("  ", "");

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse()
            .bufferFactory()
            .wrap(json.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
