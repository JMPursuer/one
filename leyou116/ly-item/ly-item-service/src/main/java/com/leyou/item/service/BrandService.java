package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.exception.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.domain.Brand;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.mapper.BrandMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
@Transactional
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<BrandDTO> brandPageQuery(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        //设置分页参数
        PageHelper.startPage(page, rows);
        //封装条件总对象
        Example example = new Example(Brand.class);
        //封装条件对象
        if(StringUtils.isNotBlank(key)){
            Example.Criteria criteria = example.createCriteria();
            criteria.orLike("name", "%"+key+"%");
            criteria.orEqualTo("letter", key.toUpperCase());
        }
        //封装排序，使用的是原始sql语句 order by id desc
        example.setOrderByClause(sortBy+" "+ (desc ? "DESC" : "ASC"));
        //分页查询
        List<Brand> brands = brandMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //得到PageHelper的分页对象
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        //得到总记录数
        Long total = pageInfo.getTotal();
        //得到BrandDTO的集合
        List<BrandDTO> brandDTOS = BeanHelper.copyWithCollection(pageInfo.getList(), BrandDTO.class);
        //创建自己的分页对象
        PageResult<BrandDTO> pageResult = new PageResult<BrandDTO>(total, brandDTOS);
        return pageResult;
    }

    public void saveBrand(BrandDTO brandDTO, List<Long> cids) {
        try {
            //保存品牌
            Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
            //保存完毕后，Brand对象中就有id的值了
            brandMapper.insertSelective(brand);
            //保存中间表
            brandMapper.saveCategoryBrand(brand.getId(), cids);
        }catch (Exception e){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    public BrandDTO findBrandById(Long id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if(brand==null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyProperties(brand, BrandDTO.class);
    }

    public List<BrandDTO> findBrandByCategoryId(Long id) {
        List<Brand> list = brandMapper.findBrandByCategoryId(id);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, BrandDTO.class);
    }
}
