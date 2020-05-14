package com.cointiger.gateway.filter;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author xuweijie
 * 此方法主要作用于处理返回数据的完整性，以保证数据全部返回给客户端
 * fluxBody返回体会存在分段传输，从而导致返回的json报文只有一半，前端无法解析而导致错误
 */


@Slf4j
public class WrapperResFilter implements GlobalFilter, Ordered {
    /**
     * 将 List 数据以""分隔进行拼接
     */
    private static Joiner joiner = Joiner.on("");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取response的 返回数据
        ServerHttpResponse originalResponse = exchange.getResponse();
        HttpHeaders header = originalResponse.getHeaders();
        header.add("Content-Type", "application/json; charset=UTF-8");

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                log.info("body:{},getStatusCode():{}", body, getStatusCode());
                if (getStatusCode() != null && getStatusCode().equals(HttpStatus.OK) && body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {//解决返回体分段传输
                        List<String> list = Lists.newArrayList();
                        dataBuffers.forEach(dataBuffer -> {
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            DataBufferUtils.release(dataBuffer);
                            try {
                                list.add(new String(content, "utf-8"));
                            } catch (Exception e) {
                                log.info("--list.add--error", e);
                            }
                        });
                        String responseData = joiner.join(list);
                        String str = new String(responseData.getBytes(), Charset.forName("UTF-8"));
                        byte[] contentBytes = str.getBytes();
                        originalResponse.getHeaders().setContentLength(contentBytes.length);
                        return originalResponse.bufferFactory().wrap(contentBytes);
                    }));
                }
                return super.writeWith(body);
            }
        };
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
