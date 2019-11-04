package com.leyou.auth.controller;

import com.leyou.auth.dto.UserInfo;
import com.leyou.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam("username") String username,
                                      @RequestParam("password") String password,
                                      HttpServletResponse response){
        authService.login(username, password, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verifyUser(HttpServletRequest request, HttpServletResponse response){
        UserInfo userInfo = authService.verifyUser(request, response);
        return ResponseEntity.ok(userInfo);
    }



}
