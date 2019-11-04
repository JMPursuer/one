package com.leyou.search.dto;

import lombok.Data;

/**
 * @author 黑马程序员
 */
@Data
public class GoodsDTO {
    private Long id; // spuId
    private String subTitle;// 卖点
    private String skus;// sku信息的json结构
}