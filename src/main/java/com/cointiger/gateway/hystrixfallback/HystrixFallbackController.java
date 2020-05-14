package com.cointiger.gateway.hystrixfallback;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xuweijie
 */

@RestController
@RequestMapping("/fallback")
public class HystrixFallbackController {

    @RequestMapping("")
    @ResponseBody
    public String fallback(){
        return "服务调用失败。。。此处为Hystrix fallback";
    }
}
