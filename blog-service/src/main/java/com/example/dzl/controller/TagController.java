package com.example.dzl.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dzl.mapstruct.BlogConvert;
import com.example.dzl.model.req.PageReq;
import com.example.dzl.model.vo.TagVO;
import com.example.dzl.service.TagService;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blog/tag")
@Slf4j
public class TagController {
    @Autowired
    private BlogConvert blogConvert;

    @Autowired
    private TagService tagService;

    /**
     * 获取标签列表（分页）
     */
    @PostMapping("/list")
    public Result<Page<TagVO>> list(@RequestBody PageReq req) {
        log.info("获取标签列表请求: {}", req);
        return Result.success(blogConvert.tagPageToVOPage(tagService.page(new Page<>(req.getPageNum(), req.getPageSize()))));
    }
}

