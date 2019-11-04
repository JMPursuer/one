package com.leyou.item.test;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncodeTest {

    //$2a$10$ITr1rE2KOkUl9gG0TlBqk.1RkOnpM5cj.vMW/aGfC8GIinBKgWD/a
    //$2a$10$siTiJHNCLB4B5hsjdQr.cuvLIxskmwmsYlA3zznp/w0JS7HaYMSL2
    @Test
    public void encodePassword(){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String psd = passwordEncoder.encode("123");
        System.out.println(psd);
    }
}
