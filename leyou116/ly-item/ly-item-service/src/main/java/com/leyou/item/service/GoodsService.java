package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.exception.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.domain.Sku;
import com.leyou.item.domain.Spu;
import com.leyou.item.domain.SpuDetail;
import com.leyou.item.dto.*;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.leyou.common.constants.MQConstants.Exchange.ITEM_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_DOWN_KEY;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_UP_KEY;

@Service
@Transactional
public class GoodsService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * @param saleable java端是Boolean类型对应mysql数据的tinyint(1)
     *                 java端为true翻译为数据库的1，为false翻译为0
     *                 数据库端0翻译为java的false，非0都是true。
     * @return
     */
    public PageResult<SpuDTO> spuPageQuery(Integer page, Integer rows, String key, Boolean saleable) {
        //设置分页参数
        PageHelper.startPage(page, rows);
        //得到封装参数的对象
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("name", "%"+key+"%");
        }
        if(saleable!=null){
            criteria.andEqualTo("saleable", saleable);
        }
        //查询数据库
        List<Spu> spus = spuMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //得到pageHelper分页对象
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        List<Spu> list = pageInfo.getList();
        //得到spudto的集合
        List<SpuDTO> spudtos = BeanHelper.copyWithCollection(list, SpuDTO.class);
        //给集合中的brandname和categoryname赋值
        handlerBrandNameAndCategoryName(spudtos);
        PageResult<SpuDTO> pageResult = new PageResult<>(pageInfo.getTotal(), pageInfo.getPages(), spudtos);
        return pageResult;
    }

    private void handlerBrandNameAndCategoryName(List<SpuDTO> spudtos) {
        spudtos.forEach(spuDTO -> {//遍历List<SpuDTO>集合，其中临时变量为spuDTO
            String categoryNames = categoryService.findCategorysByIds(spuDTO.getCategoryIds())//得到分类的集合
                    .stream()//将分类集合转成流
                    .map(CategoryDTO::getName)//开始收集name
                    .collect(Collectors.joining("/"));//收集成以/分割的字符串
            spuDTO.setCategoryName(categoryNames);

            BrandDTO brandDTO = brandService.findBrandById(spuDTO.getBrandId());
            spuDTO.setBrandName(brandDTO.getName());
        });
    }

    public void saveGoods(SpuDTO spuDTO) {
        try {
            //保存Spu
            //将dto转成pojo
            Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
            //指定新保存的商品是下架的状态
            spu.setSaleable(false);
            //保存操作
            spuMapper.insertSelective(spu);

            //保存SpuDetail
            //得到SpuDetailDTO
            SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
            //将dto转成pojo
            SpuDetail spuDetail = BeanHelper.copyProperties(spuDetailDTO, SpuDetail.class);
            //设置外键
            spuDetail.setSpuId(spu.getId());
            //保存操作
            spuDetailMapper.insertSelective(spuDetail);

            //保存Sku集合
            List<SkuDTO> skuDTOS = spuDTO.getSkus();
            //将dto集合转成pojo集合
            List<Sku> skus = BeanHelper.copyWithCollection(skuDTOS, Sku.class);
            //给sku集合中的对象添加spuId
            skus.forEach(sku -> {
                sku.setSpuId(spu.getId());
                sku.setCreateTime(new Date());
                sku.setUpdateTime(new Date());
            });
            //保存操作
            skuMapper.insertList(skus);
        }catch (Exception e){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }

    public void updateSaleable(Long id, Boolean saleable) {
        try {
            Spu record = new Spu();
            record.setId(id);
            record.setSaleable(saleable);
            spuMapper.updateByPrimaryKeySelective(record);

            //商品上下架操作引起索引库变化和静态详情页的增删是采用异步消息的方式来实现
            //上架，需要在索引库中添加一条记录，新增一个静态页面，向消息队列中发送消息
            //下架，把索引库对应的数据删除，删除一个静态页面，向消息对象发送消息
            //参数顺序依次为：交换机，routingKey，消息
            String routingKey = saleable ? ITEM_UP_KEY : ITEM_DOWN_KEY;
            amqpTemplate.convertAndSend(ITEM_EXCHANGE_NAME, routingKey, id);
        }catch (Exception e){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    public SpuDetailDTO findSpuDetailBySpuId(Long id) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        if(spuDetail==null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyProperties(spuDetail, SpuDetailDTO.class);
    }

    public List<SkuDTO> findSkudtosBySpuId(Long id) {
        Sku record = new Sku();
        record.setSpuId(id);
        List<Sku> skus = skuMapper.select(record);
        if(CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(skus, SkuDTO.class);
    }

    public void updateGoods(SpuDTO spuDTO) {
        //修改spu
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
        spuMapper.updateByPrimaryKeySelective(spu);

        //修改spuDetail
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        //将dto转成pojo
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDetailDTO, SpuDetail.class);
        spuDetailMapper.updateByPrimaryKeySelective(spuDetail);

        //删除spu下所有sku
        Sku record = new Sku();
        record.setSpuId(spu.getId());
        skuMapper.delete(record);

        //保存Sku集合
        List<SkuDTO> skuDTOS = spuDTO.getSkus();
        //将dto集合转成pojo集合
        List<Sku> skus = BeanHelper.copyWithCollection(skuDTOS, Sku.class);
        //给sku集合中的对象添加spuId
        skus.forEach(sku -> {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setUpdateTime(new Date());
        });
        //保存操作
        skuMapper.insertList(skus);
    }

    public SpuDTO findSpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu==null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        SpuDTO spuDTO = BeanHelper.copyProperties(spu, SpuDTO.class);
        //给SpuDTO对象封装SpuDetail
        SpuDetailDTO spuDetailDTO = findSpuDetailBySpuId(spuDTO.getId());
        spuDTO.setSpuDetail(spuDetailDTO);
        //给SpuDTO对象封装Sku集合
        List<SkuDTO> skuDTOS = findSkudtosBySpuId(spuDTO.getId());
        spuDTO.setSkus(skuDTOS);
        return spuDTO;
    }
}
