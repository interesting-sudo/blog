package com.example.dzl.controller;

import com.example.dzl.mapstruct.BlogConvert;
import com.example.dzl.model.vo.CategoryVO;
import com.example.dzl.service.CategoryService;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/blog/category")
@Slf4j
public class CategoryController {
    @Autowired
    private BlogConvert blogConvert;

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表
     */
    @GetMapping("/list")
    public Result<List<CategoryVO>> list() {
        log.info("获取分类列表");
        return Result.success(blogConvert.categoryListToVOList(categoryService.list()));
    }
}
