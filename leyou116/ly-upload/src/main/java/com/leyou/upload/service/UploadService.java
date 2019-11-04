package com.leyou.upload.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.exception.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class UploadService {

    private static final String IMAGE_PATH = "D:\\ly_guangzhou116\\software\\nginx-1.16.0\\html\\img-file";

    private static final String IMAGE_URL = "http://image.leyou.com/img-file/";

    //mime类型，是浏览器识别的文件类型
    private static final List ALLOW_IMAGE_TYPE = Arrays.asList("image/jpeg");

    @Autowired
    private OSS client;

    @Autowired
    private OSSProperties prop;

    public String localFileUpload(MultipartFile file) {
        //判断文件的类型
        if(!ALLOW_IMAGE_TYPE.contains(file.getContentType())){
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        //解析图片元素
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        if(bufferedImage==null){
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        //指定上传的文件名称
        String imageName = UUID.randomUUID()+file.getOriginalFilename();
        //得到上传文件的对象
        File imagePathFile = new File(IMAGE_PATH);
        //本地上传文件，上传到imagePathFile文件中，名称叫imageName
        try {
            file.transferTo(new File(imagePathFile, imageName));
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
        return IMAGE_URL+imageName;
    }

    public Map<String, Object> getOssSignature() {
        try {
            long expireTime = prop.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, prop.getMaxFileSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, prop.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<String, Object>();
            respMap.put("accessId", prop.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", prop.getDir());
            respMap.put("host", prop.getHost());
            respMap.put("expire", expireEndTime);
            return respMap;
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }

    }
}
