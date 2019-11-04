package com.leyou.item.mapper;

import com.leyou.item.domain.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand>, IdsMapper<Brand>, IdListMapper<Brand, Long>, InsertListMapper<Brand> {

    public void saveCategoryBrand(@Param("bid") Long id, @Param("cids") List<Long> cids);

    @Select("SELECT * FROM tb_brand b, tb_category_brand cb " +
            "WHERE b.id = cb.brand_id AND cb.category_id = #{id}")
    public List<Brand> findBrandByCategoryId(Long id);
}
