package com.leyou.upload.controller;

import com.leyou.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @PostMapping("/image")
    public ResponseEntity<String> localFileUpload(@RequestParam("file") MultipartFile file){
        String imageUrl = uploadService.localFileUpload(file);
        return ResponseEntity.ok(imageUrl);
    }


    @GetMapping("/signature")
    public ResponseEntity<Map<String, Object>> getOssSignature(){
        Map<String, Object> map = uploadService.getOssSignature();
        return ResponseEntity.ok(map);
    }

}
