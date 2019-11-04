package com.leyou.item.service;

import com.leyou.common.exception.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.domain.Category;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public List<CategoryDTO> findCategoryByPid(Long pid) {
        Category record = new Category();
        record.setParentId(pid);
        //得到数据库中的分类列表
        List<Category> categoryList = categoryMapper.select(record);
        //判断列表是否有数据
        if(CollectionUtils.isEmpty(categoryList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //将数据库中的对象列表转成dto列表
        return BeanHelper.copyWithCollection(categoryList, CategoryDTO.class);
    }

    public List<CategoryDTO> findCategorysByIds(List ids) {
        List list = categoryMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, CategoryDTO.class);
    }
}
