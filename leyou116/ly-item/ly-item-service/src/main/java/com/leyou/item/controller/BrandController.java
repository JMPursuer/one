package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping("/page")
    public ResponseEntity<PageResult<BrandDTO>> brandPageQuery(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc
    ){
        PageResult<BrandDTO> result = brandService.brandPageQuery(key, page, rows, sortBy, desc);
        return ResponseEntity.ok(result);
    }

    /**
     * @param brandDTO
     * @param cids @RequestParam("cids")可以将 逗号分隔的字符串直接转成list集合
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(BrandDTO brandDTO, @RequestParam("cids")List<Long> cids){
        brandService.saveBrand(brandDTO, cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> findBrandById(@PathVariable("id") Long id){
        BrandDTO brandDTO = brandService.findBrandById(id);
        return ResponseEntity.ok(brandDTO);
    }

    @GetMapping("/of/category")
    public ResponseEntity<List<BrandDTO>> findBrandByCategoryId(@RequestParam("id") Long id){
        List<BrandDTO> list = brandService.findBrandByCategoryId(id);
        return ResponseEntity.ok(list);
    }


}
