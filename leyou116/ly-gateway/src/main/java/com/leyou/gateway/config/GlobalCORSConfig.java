package com.leyou.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCORSConfig {

    @Autowired
    private CORSProperties prop;

    /**
     * @Bean注解表示自动执行当前方法，而且会把方法的返回值对象放入到IOC容器中
     * 如果当前方法有参数，会自动去IOC容器中寻找同类型的对象，作为参数使用。
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        //1.添加CORS配置信息
        CorsConfiguration config = new CorsConfiguration();
        //1) 允许的域,不要写*，否则cookie就无法使用了
        prop.getAllowedOrigins().forEach(config::addAllowedOrigin);
        //上面的写法等于
//        for (String allowedOrigin : prop.getAllowedOrigins()) {
//            config.addAllowedOrigin(allowedOrigin);
//        }
        //2) 是否发送Cookie信息
        config.setAllowCredentials(prop.getAllowedCredentials());
        //3) 允许的请求方式
        prop.getAllowedMethods().forEach(config::addAllowedMethod);
        // 4）允许的头信息
        prop.getAllowedHeaders().forEach(config::addAllowedHeader);
        // 5）有效期
        config.setMaxAge(prop.getMaxAge());

        //2.添加映射路径，我们拦截一切请求
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration(prop.getFilterPath(), config);

        //3.返回新的CORSFilter.
        return new CorsFilter(configSource);
    }
}