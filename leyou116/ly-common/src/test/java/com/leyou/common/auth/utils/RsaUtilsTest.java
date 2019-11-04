package com.leyou.common.auth.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

public class RsaUtilsTest {

    private static final String pubPath = "D:\\ly_guangzhou116\\ssh\\rsa_key.pub";
    private static final String priPath = "D:\\ly_guangzhou116\\ssh\\rsa_key";

    @Test
    public void generateKey() throws Exception {
        RsaUtils.generateKey(pubPath, priPath, "heima", 2048);
    }

    @Test
    public void buildJwt() throws Exception {
        //得到私钥
        PrivateKey privateKey = RsaUtils.getPrivateKey(priPath);
        String jws = Jwts.builder()
                .setSubject("Joe")
                .signWith(privateKey)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+111111111))
                .claim("role", "admin")
                .compact();
        System.out.println(jws);
    }

    @Test
    public void parseJwt() throws Exception {
        //得到公钥
        PublicKey publicKey = RsaUtils.getPublicKey(pubPath);
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJKb2UiLCJpYXQiOjE1NzI2NjA5ODksImV4cCI6MTU3Mjc3MjEwMCwicm9sZSI6ImFkbWluIn0.vAKJZx4zbVTAaZGfVMJowPVA5jwZNgZ77jWGMl3QT6mlD4SPorpU6lkjQhb6mNUeu0irDBOq2QOoRf7m0wWM976VSvLAN1OHMnFmf-nNmRDXCmAxL9zNgOezc1HNjxPeQYZ5nGyzkCpd0h5qXU9yJqkw7H5X9lG40EWCWoWYZqjSERlllIfqCO2LB_QPyG2eFvDNxydlvGeA91tchJzvNfZ49nqI__VNfLQ-MfhkAMs_T4wdFjk8vyiDrTnP5t7tJ6Vc1fYIdww9k-HvpxBki_5vmTNYlZJff9AOO5szQl8M07N3xoIZ_tXCaxVEaYIvQqJV6Ul9ANRJqtW7eLwTBA";
        Claims claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token).getBody();
        System.out.println(claims.getSubject());
        System.out.println(claims.getIssuedAt());
        System.out.println(claims.getExpiration());
        System.out.println(claims.get("role"));
    }

    @Test
    public void getPublicKey() throws Exception {
        PublicKey publicKey = RsaUtils.getPublicKey(pubPath);
        System.out.println(publicKey);
    }

    @Test
    public void getPrivateKey() throws Exception {
        PrivateKey privateKey = RsaUtils.getPrivateKey(priPath);
        System.out.println(privateKey);
    }


}