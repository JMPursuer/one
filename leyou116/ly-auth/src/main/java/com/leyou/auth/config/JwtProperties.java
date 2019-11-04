package com.leyou.auth.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {

    private String pubKeyPath;
    private String priKeyPath;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    private JwtUser user = new JwtUser();

    @Data
    public class JwtUser{
        private Integer expire;
        private Integer refreshTime;
        private String cookieName;
        private String cookieDomain;
    }

    //<bean id="" class="" init-method="" destroy-method=""/>
    //在对象彻底创建成功之后执行
    @PostConstruct
    public void init() throws Exception {
        publicKey = RsaUtils.getPublicKey(pubKeyPath);
        privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

}