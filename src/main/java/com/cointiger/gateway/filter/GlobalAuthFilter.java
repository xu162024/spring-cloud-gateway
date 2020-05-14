package com.cointiger.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xuweijie
 * 此方法作用于全局请求Token过滤
 */
@Slf4j
@Component
public class GlobalAuthFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String token = request.getQueryParams().getFirst("token");
        if (token != null && !token.equals("")) {
            return chain.filter(exchange);
        }

        ServerHttpResponse response = exchange.getResponse();
        //设置http响应状态码
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        //设置响应头信息Content-Type类型
        response.getHeaders().add("Content-Type", "application/json");
        //设置返回json数据
        return response.writeAndFlushWith(Flux.just(ByteBufFlux.just(response.bufferFactory().wrap(getWrapData()))));
        //直接返回（没有返回数据）
//        return response.setComplete().then();
        //设置返回的数据（非json格式）
//        return response.writeWith(Flux.just(response.bufferFactory().wrap("".getBytes())));
    }

    private byte[] getWrapData() {
        Map<String, String> map = new HashMap<>();
        map.put("code", "1");
        map.put("msg", "your token is empty or illegal");
        try {
            return new ObjectMapper().writeValueAsString(map).getBytes();
        } catch (JsonProcessingException e) {
            //
        }
        return "".getBytes();
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
