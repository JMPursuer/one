package com.leyou.search.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;
import java.util.Set;

/**
 * 一个SPU对应一个Goods
 */
@Data
@Document(indexName = "goods", type = "docs", shards = 1, replicas = 1)
public class Goods {
    @Id
    @Field(type = FieldType.Keyword)
    private Long id; // spuId
    @Field(type = FieldType.Keyword, index = false)
    private String subTitle;// 卖点
    @Field(type = FieldType.Keyword, index = false)
    private String skus;// sku信息的json结构,如果是List集合在elasticSearch中默认是索引的

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String all; // 所有需要被搜索的信息，包含标题，分类，甚至品牌
    //如果没有添加@Field注解的字段，默认都不分词但是索引
    private Long brandId;// 品牌id
    private Long categoryId;// 商品3级分类id
    private Long createTime;// spu创建时间   排序字段
    private Set<Long> price;// 价格  排序字段  如果是倒叙，优先使用高价格，正序使用小价格
    private Map<String, Object> specs;// 可搜索的规格参数，key是参数名，值是参数值
}