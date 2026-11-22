package com.driveaway.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * Global Request & Response Logger for the DriveAway API Gateway.
 * Gives real-time visibility and monitoring of all incoming traffic across microservices.
 */
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        InetSocketAddress remoteAddress = request.getRemoteAddress();
        String clientIp = (remoteAddress != null) ? remoteAddress.getAddress().getHostAddress() : "UNKNOWN";
        String method = request.getMethod().name();
        String path = request.getURI().getPath();

        log.info("🌐 [GATEWAY] ---> INCOMING: {} {} | Client IP: {}", method, path, clientIp);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = (response.getStatusCode() != null) ? response.getStatusCode().value() : 0;

            log.info("🔙 [GATEWAY] <--- COMPLETED: {} {} | Status: {} | Duration: {}ms",
                    method, path, statusCode, duration);
        }));
    }

    @Override
    public int getOrder() {
        // High precedence to log at the very entrance of the Gateway pipeline
        return -1;
    }
}
