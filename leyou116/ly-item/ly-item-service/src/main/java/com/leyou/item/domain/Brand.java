package com.leyou.item.domain;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 黑马程序员
 */
@Data
@Table(name = "tb_brand")
public class Brand {
    @Id
    @KeySql(useGeneratedKeys = true)//新增时返回新增对象的自增主键值
    private Long id;
    private String name;
    private String image;
    private Character letter;
    private Date createTime;
    private Date updateTime;
}