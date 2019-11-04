package com.leyou.upload.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 黑马程序员
 */
@Configuration
public class OSSConfig {

    @Bean
    public OSS ossClient(OSSProperties prop){
        //OSSClient client = new OSSClient(prop.getEndpoint(), prop.getAccessKeyId(), prop.getAccessKeySecret());
        return new OSSClientBuilder()
                .build(prop.getEndpoint(), prop.getAccessKeyId(), prop.getAccessKeySecret());
    }
}