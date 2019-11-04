package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/spec")
public class SpecController {

    @Autowired
    private SpecService specService;

    @GetMapping("/groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroupByCid(@RequestParam("id") Long id){
        return ResponseEntity.ok(specService.findSpecGroupByCid(id));
    }

    @GetMapping("/params")
    public ResponseEntity<List<SpecParamDTO>> findSpecParam(@RequestParam(value = "gid", required = false) Long gid,
                                                            @RequestParam(value = "cid", required = false) Long cid,
                                                            @RequestParam(value = "searching", required = false) Boolean searching){
        return ResponseEntity.ok(specService.findSpecParam(gid, cid, searching));
    }

    @GetMapping("/of/category")
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroupByCidWithParams(@RequestParam("id") Long id){
        return ResponseEntity.ok(specService.findSpecGroupByCidWithParams(id));
    }

}
