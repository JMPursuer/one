package com.leyou.item.dto;

import lombok.Data;

/**
 * 黑马程序员
 */
@Data
public class CategoryDTO {
	private Long id;
	private String name;
	private Long parentId;
	private Boolean isParent;
	private Integer sort;
}