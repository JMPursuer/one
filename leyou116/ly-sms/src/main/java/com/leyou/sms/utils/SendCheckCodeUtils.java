package com.leyou.sms.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsConstants;
import com.leyou.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SendCheckCodeUtils {

    @Autowired
    private IAcsClient client;

    @Autowired
    private SmsProperties prop;

    public void sendCheckCodeMsg(String phone, String code){

        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain(prop.getDomain());
        request.setVersion(prop.getVersion());
        request.setAction(prop.getAction());
        request.putQueryParameter(SmsConstants.SMS_PARAM_KEY_REGION_ID, prop.getRegionID());
        request.putQueryParameter(SmsConstants.SMS_PARAM_KEY_PHONE, phone);
        request.putQueryParameter(SmsConstants.SMS_PARAM_KEY_SIGN_NAME, prop.getSignName());
        request.putQueryParameter(SmsConstants.SMS_PARAM_KEY_TEMPLATE_CODE, prop.getVerifyCodeTemplate());
        request.putQueryParameter(SmsConstants.SMS_PARAM_KEY_TEMPLATE_PARAM, "{\"checkcode\":\""+code+"\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            //处理返回结果
            Map<String, String> repsData = JsonUtils.toMap(response.getData(), String.class, String.class);
            if(!StringUtils.equals(repsData.get("Code"), "OK")){
                log.error("手机号:"+phone+"，接收短信验证码失败！原因为："+repsData.get("Message"));
            }
            log.info("手机号:"+phone+"，接收短信验证码成功！");
        } catch (ServerException e) {
            log.error("阿里云短信服务器异常！");
            e.printStackTrace();
        } catch (ClientException e) {
            log.error("本地阿里云短信客户端异常！");
            e.printStackTrace();
        }
    }
}
