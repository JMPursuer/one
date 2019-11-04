package com.leyou.search.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping("/page")
    public ResponseEntity<PageResult<GoodsDTO>> goodsPageQuery(@RequestBody SearchRequest request){
        PageResult<GoodsDTO> result = searchService.goodsPageQuery(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/filter")
    public ResponseEntity<Map<String, List<?>>> filterParamQuery(@RequestBody SearchRequest request){
        Map<String, List<?>> result = searchService.filterParamQuery(request);
        return ResponseEntity.ok(result);
    }







}
