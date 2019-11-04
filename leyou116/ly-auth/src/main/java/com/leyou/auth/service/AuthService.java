package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.dto.UserInfo;
import com.leyou.common.auth.domain.Payload;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.exception.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.UserDTO;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProp;

    public void login(String username, String password, HttpServletResponse response) {
        //验证用户名和密码是否正确
        UserDTO userDTO = userClient.findByUsernameAndPassword(username, password);
        if(userDTO==null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //创建载荷使用的用户对象
        UserInfo userInfo = new UserInfo(userDTO.getId(), userDTO.getUsername(), "ROLE_ADMIN");
        //生成token并将token写入到Cookie中
        writeTokenToCookie(response, userInfo);
    }

    //生成token并将token写入到Cookie中
    private void writeTokenToCookie(HttpServletResponse response, UserInfo userInfo) {
        //生成token
        String token = JwtUtils.generateTokenExpireInMinutes(userInfo, jwtProp.getPrivateKey(), jwtProp.getUser().getExpire());
        //把token写入浏览器的Cookie中
        CookieUtils.newCookieBuilder()
                .response(response)
                .name(jwtProp.getUser().getCookieName())
                .value(token)
                .domain(jwtProp.getUser().getCookieDomain())
                .httpOnly(true)
                .build();
    }

    public UserInfo verifyUser(HttpServletRequest request, HttpServletResponse response) {
        //获取token
        String token = CookieUtils.getCookieValue(request, jwtProp.getUser().getCookieName());
        if(StringUtils.isBlank(token)){
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        //校验token是否合法
        Payload<UserInfo> infoPayload = null;
        try {
            infoPayload = JwtUtils.getInfoFromToken(token, jwtProp.getPublicKey(), UserInfo.class);
        }catch (Exception e){
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        //获取UserInfo
        UserInfo userInfo = infoPayload.getUserInfo();

        //得到当前token的过期时间
        Date expTime = infoPayload.getExpiration();
        //得到刷新token的时间点
        DateTime refreshTime = new DateTime(expTime).minusMinutes(jwtProp.getUser().getRefreshTime());
        //如果当前时间在刷新时间之后触发刷新token操作
        if(refreshTime.isBefore(System.currentTimeMillis())){
            //生成token并将token写入到Cookie中
            writeTokenToCookie(response, userInfo);
        }

        return userInfo;
    }
}
