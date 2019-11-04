package com.leyou.item.controller;

import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Id;
import java.util.List;

@RestController
@RequestMapping("/category")
//@CrossOrigin("http://manage.leyou.com")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/of/parent")
    public ResponseEntity<List<CategoryDTO>> findCategoryByPid(@RequestParam("pid") Long pid){
        List<CategoryDTO> list = categoryService.findCategoryByPid(pid);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/list")
    public ResponseEntity<List<CategoryDTO>> findCategorysByIds(@RequestParam("ids") List ids){
        List<CategoryDTO> list = categoryService.findCategorysByIds(ids);
        return ResponseEntity.ok(list);
    }

}
