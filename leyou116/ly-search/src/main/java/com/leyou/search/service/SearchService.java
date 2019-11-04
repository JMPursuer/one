package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.exception.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import com.leyou.search.domain.Goods;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.repository.SearchRepository;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private ElasticsearchTemplate esTemplate;

    /**
     * 将一个数据库中的Spu对象转成索引库中的Goods对象
     * @param spuDTO
     * @return
     */
    public Goods buildGoods(SpuDTO spuDTO){
        //得到当前spu下所有的sku的集合
        List<SkuDTO> skuDTOS = itemClient.findSkudtosBySpuId(spuDTO.getId());
        //创建一个List集合，这里面只包含sku中四个属性 id, title, price, image
        List<Map<String, Object>> skuList = new ArrayList<>();
        //遍历skuDTOS给skuList赋值
        skuDTOS.forEach(skuDTO -> {
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("id", skuDTO.getId());
            skuMap.put("title", skuDTO.getTitle());
            skuMap.put("price", skuDTO.getPrice());
            skuMap.put("image", StringUtils.substringBefore(skuDTO.getImages(), ","));
            skuList.add(skuMap);
        });

        //得到所有sku的价格的集合
        Set<Long> priceSet = skuDTOS.stream().map(SkuDTO::getPrice).collect(Collectors.toSet());

        //得到规格参数的key所在对象
        List<SpecParamDTO> specParamDTOList = itemClient.findSpecParam(null, spuDTO.getCid3(), true);
        //得到规格参数的value所在对象
        SpuDetailDTO spuDetailDTO = itemClient.findSpuDetailBySpuId(spuDTO.getId());
        //获取通用规格参数的字符串
        String genericSpecStr = spuDetailDTO.getGenericSpec();
        //将字符串类型的通用规格参数转成map
        Map<Long, Object> genericSpecMap = JsonUtils.toMap(genericSpecStr, Long.class, Object.class);
        //获取特有规格参数的字符串
        String specialSpecStr = spuDetailDTO.getSpecialSpec();
        //将字符串类型的特有规格参数转成map
        Map<Long, List<String>> specialSpecMap = JsonUtils.nativeRead(specialSpecStr, new TypeReference<Map<Long, List<String>>>() {
        });

        //创建也给规格参数的map对象
        Map<String, Object> specParamMap = new HashMap<>();
        //遍历规格参数所在对象集合
        specParamDTOList.forEach(specParamDTO -> {
            String key = specParamDTO.getName();
            Object value = null;
            //判断规格参数值的来源
            if(specParamDTO.getGeneric()){
                value = genericSpecMap.get(specParamDTO.getId());
            }else {
                value = specialSpecMap.get(specParamDTO.getId());
            }
            //将目前规格参数的值中所有为数字的都转成区间来存储到索引库
            if(specParamDTO.getNumeric()){
                value = chooseSegment(value, specParamDTO);
            }
            specParamMap.put(key, value);
        });

        //创建Goods对象
        Goods goods = new Goods();
        goods.setId(spuDTO.getId());
        goods.setSubTitle(spuDTO.getSubTitle());
        goods.setCreateTime(spuDTO.getCreateTime().getTime());
        goods.setBrandId(spuDTO.getBrandId());
        goods.setCategoryId(spuDTO.getCid3());
        goods.setAll(spuDTO.getName()+spuDTO.getBrandName()+spuDTO.getCategoryName());
        goods.setSkus(JsonUtils.toString(skuList));
        goods.setPrice(priceSet);
        goods.setSpecs(specParamMap);
        return goods;
    }

    //将目前规格参数的值中所有为数字的都转成区间来存储到索引库
    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 加载搜索页面商品分页列表
     * @param request
     * @return
     */
    public PageResult<GoodsDTO> goodsPageQuery(SearchRequest request) {
        //提供一个可以封装各种复杂条件的查询构建器
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        //指定要查询的字段域
        searchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"}, null));
        //封装分页信息
        searchQueryBuilder.withPageable(PageRequest.of(request.getPage()-1, request.getSize()));
        //封装查询条件
        searchQueryBuilder.withQuery(buildSearchKey(request));
        AggregatedPage<Goods> goodsPageResult = esTemplate.queryForPage(searchQueryBuilder.build(), Goods.class);
        //得到分页中的Goods集合
        List<Goods> goodsList = goodsPageResult.getContent();
        if(CollectionUtils.isEmpty(goodsList)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return new PageResult<>(goodsPageResult.getTotalElements(),
                goodsPageResult.getTotalPages(),
                BeanHelper.copyWithCollection(goodsList, GoodsDTO.class));
    }

    //构建查询条件
    private QueryBuilder buildSearchKey(SearchRequest request) {
        //创建一个组合条件查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //向组合条件查询中封装搜索条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
        //向组合条件查询中封装过滤条件
        Map<String, Object> filterParamsMap = request.getFilterParams();
        filterParamsMap.entrySet().forEach(entry->{
            String field = entry.getKey();
            Object value = entry.getValue();
            //要对域字段进行处理
            if(field.equals("分类")){
                field = "categoryId";
            }else if (field.equals("品牌")){
                field = "brandId";
            }else {
                field = "specs."+field;
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(field, value));
        });
        return boolQueryBuilder;
    }

    /**
     * 加载搜索页面过滤条件
     * @param request
     * @return
     */
    public Map<String, List<?>> filterParamQuery(SearchRequest request) {
        Map<String, List<?>> filterParamMap = new LinkedHashMap<>();

        //提供一个封装条件的对象
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();

        //优化查询部分
        searchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
        searchQueryBuilder.withPageable(PageRequest.of(0, 1));

        //添加查询条件
        searchQueryBuilder.withQuery(buildSearchKey(request));

        //添加分类聚合条件
        String categoryAgg = "categoryAgg";
        searchQueryBuilder.addAggregation(AggregationBuilders.terms(categoryAgg).field("categoryId"));

        //添加品牌聚合条件
        String brandAgg = "brandAgg";
        searchQueryBuilder.addAggregation(AggregationBuilders.terms(brandAgg).field("brandId"));

        //索引库查询
        AggregatedPage<Goods> goodsAggregatedResult = esTemplate.queryForPage(searchQueryBuilder.build(), Goods.class);

        //解析结果将分类结果放入到filterParamMap中
        Terms categoryTerms = (Terms) goodsAggregatedResult.getAggregation(categoryAgg);
        List<Long> categoryIds = handlerCategoryParam(categoryTerms, filterParamMap);

        //解析结果将品牌结果放入到filterParamMap中
        Aggregations aggregations = goodsAggregatedResult.getAggregations();
        Terms brandTerms = aggregations.get(brandAgg);
        handlerBrandParam(brandTerms, filterParamMap);

        //将规格参数过滤条件加入到filterParamMap中
        addSpecParamFilter(filterParamMap, categoryIds, buildSearchKey(request));
        return filterParamMap;
    }

    private void addSpecParamFilter(Map<String, List<?>> filterParamMap, List<Long> categoryIds, QueryBuilder buildSearchKey) {
        categoryIds.forEach(categoryId->{
            //获取当前分类下可以被作为查询条件的规格参数
            List<SpecParamDTO> specParamDTOS = itemClient.findSpecParam(null, categoryId, true);

            //提供一个封装条件的对象
            NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();

            //优化查询部分
            searchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
            searchQueryBuilder.withPageable(PageRequest.of(0, 1));

            //添加查询条件
            searchQueryBuilder.withQuery(buildSearchKey);

            //循环添加组件查询条件
            specParamDTOS.forEach(specParamDTO -> {
                //得到组合名称
                String aggName = specParamDTO.getName();
                //得到组合所需的field字段
                String aggField = "specs."+aggName;
                //添加组合条件
                searchQueryBuilder.addAggregation(AggregationBuilders.terms(aggName).field(aggField));
            });

            //索引库查询
            AggregatedPage<Goods> goodsAggregatedResult = esTemplate.queryForPage(searchQueryBuilder.build(), Goods.class);

            //得到所有的组合结果
            Aggregations aggregations = goodsAggregatedResult.getAggregations();

            //遍历解析出聚合结果
            specParamDTOS.forEach(specParamDTO -> {
                //得到组合名称
                String aggName = specParamDTO.getName();
                //根据聚合后桶的名字得到桶
                Terms terms = aggregations.get(aggName);
                //解析桶中数据的集合
                List<String> specList = terms.getBuckets().stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                //将得到的规格参数集合放入到filterParamMap中
                filterParamMap.put(aggName, specList);
            });
        });
    }

    private void handlerBrandParam(Terms brandTerms, Map<String, List<?>> filterParamMap) {
        List<BrandDTO> brandDTOS = brandTerms.getBuckets()
                .stream()
                .map(Terms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .map(itemClient::findBrandById)
                .collect(Collectors.toList());
        filterParamMap.put("品牌", brandDTOS);
    }

    private List<Long> handlerCategoryParam(Terms categoryTerms, Map<String, List<?>> filterParamMap) {
        List<Long> categoryIds = categoryTerms.getBuckets()
                .stream()
                .map(Terms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        //通过所有分类的id的集合查询出分类对象的集合
        List<CategoryDTO> categoryDTOS = itemClient.findCategorysByIds(categoryIds);
        filterParamMap.put("分类", categoryDTOS);
        return categoryIds;
    }

    public void addIndex(Long id) {
        SpuDTO spuDTO = itemClient.findSpuById(id);
        Goods goods = buildGoods(spuDTO);
        searchRepository.save(goods);
    }

    public void delIndex(Long id) {
        searchRepository.deleteById(id);
    }

}
