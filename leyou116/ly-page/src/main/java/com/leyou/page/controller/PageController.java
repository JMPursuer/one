package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("/item/{id}.html")
    public String toItemPage(Model model, @PathVariable("id") Long id){
        Map<String, Object> itemMap = pageService.findItemMapData(id);
        model.addAllAttributes(itemMap);
        return "item";
    }

}
