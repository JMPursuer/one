package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuDTO>> spuPageQuery(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                           @RequestParam(value = "rows", defaultValue = "5") Integer rows,
                                                           @RequestParam(value = "key", required = false) String key,
                                                           @RequestParam(value = "saleable", required = false) Boolean saleable){
        PageResult<SpuDTO> pageResult = goodsService.spuPageQuery(page, rows, key, saleable);
        return ResponseEntity.ok(pageResult);
    }

    @PostMapping("/goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO){
        goodsService.saveGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/spu/saleable")
    public ResponseEntity<Void> updateSaleable(@RequestParam("id") Long id,
                                               @RequestParam("saleable") Boolean saleable){
        goodsService.updateSaleable(id, saleable);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/spu/detail")
    public ResponseEntity<SpuDetailDTO> findSpuDetailBySpuId(@RequestParam("id") Long id){
        SpuDetailDTO spuDetailDTO = goodsService.findSpuDetailBySpuId(id);
        return ResponseEntity.ok(spuDetailDTO);
    }

    @GetMapping("/sku/of/spu")
    public ResponseEntity<List<SkuDTO>> findSkudtosBySpuId(@RequestParam("id") Long id){
        List<SkuDTO> list = goodsService.findSkudtosBySpuId(id);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spuDTO){
        goodsService.updateGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/spu/{id}")
    public ResponseEntity<SpuDTO> findSpuById(@PathVariable("id") Long id){
        SpuDTO spuDTO = goodsService.findSpuById(id);
        return ResponseEntity.ok(spuDTO);
    }

}
