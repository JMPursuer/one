package com.leyou.user.service;

import com.leyou.common.constants.MQConstants;
import com.leyou.common.exception.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.user.domain.User;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.mapper.UserMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final String CHECK_CODE_PRE = "check:code:pre:";

    public void register(User user, String code) {
        //获取redis中的验证码
        String redisCode = redisTemplate.opsForValue().get(CHECK_CODE_PRE + user.getPhone());
        //校验验证码是否正确【现在还没验证码】
        if(!StringUtils.equals(code, redisCode)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        //保存数据
        userMapper.insertSelective(user);
    }

    public Boolean checkUsernameOrPhone(String data, Integer type) {
        User record = new User();
        switch (type) {
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        int count = userMapper.selectCount(record);
        return count==0;
    }

    public void sendCheckCode(String phone) {
        //生成验证码
        String checkCode = RandomStringUtils.randomNumeric(6);
        //存入redis中
        redisTemplate.opsForValue().set(CHECK_CODE_PRE+phone, checkCode, 6, TimeUnit.HOURS);
        //向mq中发送一个
        Map<String, String> codeMap = new HashMap<>();
        codeMap.put("phone", phone);
        codeMap.put("code", checkCode);
        amqpTemplate.convertAndSend(MQConstants.Exchange.SMS_EXCHANGE_NAME,
                MQConstants.RoutingKey.VERIFY_CODE_KEY,
                codeMap);
    }

    public UserDTO findByUsernameAndPassword(String username, String password) {
        try {
            User record = new User();
            record.setUsername(username);
            //先根据用户名查询用户对象
            User user = userMapper.selectOne(record);

            //如果密码也正确则返回dto对象
            if(passwordEncoder.matches(password, user.getPassword())){
                return BeanHelper.copyProperties(user, UserDTO.class);
            }

            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }catch (Exception e){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

    }

}
