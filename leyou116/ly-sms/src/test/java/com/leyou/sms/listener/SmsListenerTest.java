package com.leyou.sms.listener;

import com.leyou.common.constants.MQConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsListenerTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void sendCheckCodeMsg() {
        Map<String, String> codeMap = new HashMap<>();
        codeMap.put("phone", "15797678485");
        codeMap.put("code", "666");
        amqpTemplate.convertAndSend(MQConstants.Exchange.SMS_EXCHANGE_NAME,
                MQConstants.RoutingKey.VERIFY_CODE_KEY, codeMap);
    }
}