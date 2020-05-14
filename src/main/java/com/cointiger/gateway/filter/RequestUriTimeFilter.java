package com.cointiger.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author xuweijie
 * 此方法主要作用于打印网关请求时间日志
 */
@Slf4j
@Component
public class RequestUriTimeFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //记录请求开始时间
        exchange.getAttributes().put(REQUEST_TIME_BEGIN, System.currentTimeMillis());

        return chain.filter(exchange).then(Mono.fromRunnable(new Runnable() {
            @Override
            public void run() {
                Long startTime = exchange.getAttribute(REQUEST_TIME_BEGIN);
                if (startTime != null) {
                    //打印
                    log.info(exchange.getRequest().getURI() + " 耗时" + (System.currentTimeMillis() - startTime));
                }
            }
        }));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
