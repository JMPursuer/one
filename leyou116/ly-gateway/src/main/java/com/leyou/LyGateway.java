package com.leyou;

import com.leyou.gateway.config.CORSProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringCloudApplication//这里面包含了下面两个注解
//@SpringBootApplication
//@EnableDiscoveryClient
@EnableZuulProxy//生命当前是个网关的启动类
@EnableConfigurationProperties(CORSProperties.class)
public class LyGateway {
    public static void main(String[] args) {
        SpringApplication.run(LyGateway.class, args);
    }
}
