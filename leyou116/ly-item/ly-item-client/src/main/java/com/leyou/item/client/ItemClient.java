package com.leyou.item.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("item-service")
public interface ItemClient {
    @GetMapping("/spu/page")
    public PageResult<SpuDTO> spuPageQuery(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                           @RequestParam(value = "rows", defaultValue = "5") Integer rows,
                                                           @RequestParam(value = "key", required = false) String key,
                                                           @RequestParam(value = "saleable", required = false) Boolean saleable);

    @GetMapping("/sku/of/spu")
    public List<SkuDTO> findSkudtosBySpuId(@RequestParam("id") Long id);

    @GetMapping("/spu/detail")
    public SpuDetailDTO findSpuDetailBySpuId(@RequestParam("id") Long id);

    @GetMapping("/spec/params")
    public List<SpecParamDTO> findSpecParam(@RequestParam(value = "gid", required = false) Long gid,
                                                            @RequestParam(value = "cid", required = false) Long cid,
                                                            @RequestParam(value = "searching", required = false) Boolean searching);

    /**
     * @param ids  注意feign调用如果参数为集合，必须加泛型
     * @return
     */
    @GetMapping("/category/list")
    public List<CategoryDTO> findCategorysByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/brand/{id}")
    public BrandDTO findBrandById(@PathVariable("id") Long id);

    @GetMapping("/spu/{id}")
    public SpuDTO findSpuById(@PathVariable("id") Long id);

    @GetMapping("/spec/of/category")
    public List<SpecGroupDTO> findSpecGroupByCidWithParams(@RequestParam("id") Long id);

}
