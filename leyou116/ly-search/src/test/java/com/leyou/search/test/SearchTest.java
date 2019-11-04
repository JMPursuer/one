package com.leyou.search.test;

import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.domain.Goods;
import com.leyou.search.repository.SearchRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchTest {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private SearchService searchService;

    @Autowired
    private SearchRepository searchRepository;

    @Test
    public void paramsTest(){
        List<SpecParamDTO> specParam = itemClient.findSpecParam(null, 76L, null);
        System.out.println(specParam);
    }

    @Test
    public void writeIndex(){
        int page=1, rows=100, totalPage=1;
        do{
            PageResult<SpuDTO> pageResult = itemClient.spuPageQuery(page, rows, null, true);
            //得到当前页的spu集合
            List<SpuDTO> items = pageResult.getItems();
            items.forEach(item->{
                //将spu转成Goods
                Goods goods = searchService.buildGoods(item);
                searchRepository.save(goods);
            });
            totalPage = pageResult.getTotalPage();
            page++;
        }while (page <= totalPage);

    }




}
