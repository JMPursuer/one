package com.leyou.item.service;

import com.leyou.common.exception.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.domain.SpecGroup;
import com.leyou.item.domain.SpecParam;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SpecService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroupDTO> findSpecGroupByCid(Long id) {
        SpecGroup record = new SpecGroup();
        record.setCid(id);
        List<SpecGroup> list = specGroupMapper.select(record);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, SpecGroupDTO.class);
    }

    public List<SpecParamDTO> findSpecParam(Long gid, Long cid, Boolean searching) {
        //要求规格组和商品分类至少有一个
        if(cid==null && gid==null){
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        SpecParam record = new SpecParam();
        record.setCid(cid);
        record.setGroupId(gid);
        record.setSearching(searching);
        List<SpecParam> list = specParamMapper.select(record);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, SpecParamDTO.class);
    }

    public List<SpecGroupDTO> findSpecGroupByCidWithParams(Long id) {
        //根据分类id查询规格组的集合
        List<SpecGroupDTO> specGroupDTOS = findSpecGroupByCid(id);
        //根据分类id查询出规格参数的集合
        List<SpecParamDTO> specParamDTOS = findSpecParam(null, id, null);
        //先把规格参数集合按照规格组id来分组
        Map<Long, List<SpecParamDTO>> paramsMap = specParamDTOS.stream().collect(Collectors.groupingBy(SpecParamDTO::getGroupId));
        //变量规格组，给其规格参数集合属性赋值
        specGroupDTOS.forEach(specGroupDTO -> {
            specGroupDTO.setParams(paramsMap.get(specGroupDTO.getId()));
        });

        //变量规格组，给其规格参数集合属性赋值【凑合用】
        //long可以用等号 Long在127到-128之间可以用等号，超出这个范围其实比较是两个对象的地址
//        specGroupDTOS.forEach(specGroupDTO -> {
//            specParamDTOS.forEach(specParamDTO -> {
//                //判断规格参数集合中的规格组id与当前规格组id相等的，赋值给当前规格组
//                if(specGroupDTO.getId()==specParamDTO.getGroupId()){
//                    specGroupDTO.getParams().add(specParamDTO);
//                }
//            });
//        });

        //在循环内去调用数据库，效率非常差【不推荐】
//        specGroupDTOS.forEach(specGroupDTO -> {
//            Long gid = specGroupDTO.getId();
//            List<SpecParamDTO> specParamDTOS = findSpecParam(gid, null, null);
//            specGroupDTO.setParams(specParamDTOS);
//        });

        return specGroupDTOS;
    }
}
