package com.leyou.user.domain;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.Date;

@Table(name = "tb_user")
@Data
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    @Length(min = 4, max = 18, message = "无效的用户名")
    private String username;
    @Size(min = 4, max = 18, message = "无效的密码")
    private String password;
    private String phone;
    private Date createTime;
    private Date updateTime;
}