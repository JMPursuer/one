package com.leyou.user.controller;

import com.leyou.common.exception.LyException;
import com.leyou.user.domain.User;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUsernameOrPhone(@PathVariable("data") String data,
                                                        @PathVariable("type") Integer type){
        Boolean result = userService.checkUsernameOrPhone(data, type);
        return  ResponseEntity.ok(result);
    }

    @PostMapping("/code")
    public ResponseEntity<Void> sendCheckCode(@RequestParam("phone") String phone){
        userService.sendCheckCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * @param user 必须加@Valid才能使用hibernate的验证
     * @param result BindingResult必须放在要验证的对象后面，才能接收验证结果
     * @param code
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid User user,
                                         BindingResult result,
                                         @RequestParam("code") String code){
        //如果验证结果有异常，则抛出我们自定义的异常
        if(result.hasErrors()){
            String error = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("|"));
            throw new LyException(400, error);
        }
        userService.register(user, code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/query")
    public ResponseEntity<UserDTO> findByUsernameAndPassword(@RequestParam("username") String username,
                                                             @RequestParam("password") String password){
        UserDTO userDTO = userService.findByUsernameAndPassword(username, password);
        return ResponseEntity.ok(userDTO);
    }

}
