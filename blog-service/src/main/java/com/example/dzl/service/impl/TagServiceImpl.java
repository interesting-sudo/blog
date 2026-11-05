package com.example.dzl.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dzl.enums.BlogErrorEnum;
import com.example.dzl.exception.BusinessException;
import com.example.dzl.feign.SearchServiceClient;

import com.example.dzl.mapper.BlogMapper;
import com.example.dzl.mapper.BlogTagMapper;
import com.example.dzl.mapper.TagMapper;

import com.example.dzl.model.entity.Blog;
import com.example.dzl.model.entity.BlogTag;
import com.example.dzl.model.entity.Tag;
import com.example.dzl.model.req.PageReq;
import com.example.dzl.model.vo.TagAdminVO;
import com.example.dzl.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    @Autowired
    private BlogTagMapper blogTagMapper;

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private SearchServiceClient searchServiceClient;

    /**
     * 获取标签ID
     */
    @Override
    public List<Long> getOrCreateTagIds(String tagNames) {
        if(!StringUtils.hasText(tagNames)){
            return null;
        }

        // 解析标签名称
        List<String> tagNameList = Arrays.stream(tagNames.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        // 获取标签ID
        List<Long> tagIds = tagNameList.stream()
                .map(tagName -> {
                    Tag tag = this.getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, tagName));
                    if(tag == null){
                        tag = new Tag();
                        tag.setName(tagName);
                        this.save(tag);
                        log.info("创建新标签：{}", tag.getName());

                    }
                    return tag.getId();
                })
                .toList();

        return tagIds;

    }

    @Override
    public Page<TagAdminVO> pageAdmin(PageReq req) {
        Page<Tag> pageParam = new Page<>(req.getPageNum(), req.getPageSize());
        Page<Tag> tagPage = this.page(pageParam);

        List<TagAdminVO> adminVOList = tagPage.getRecords().stream().map(tag -> {
            TagAdminVO vo = new TagAdminVO();
            vo.setId(tag.getId());
            vo.setName(tag.getName());
            vo.setCreateTime(tag.getCreateTime());

            // 查询关联文章数
            Long count = blogTagMapper.selectCount(new LambdaQueryWrapper<BlogTag>()
                    .eq(BlogTag::getTagId, tag.getId()));
            vo.setBlogCount(count);
            return vo;
        }).collect(Collectors.toList());

        Page<TagAdminVO> resultPage = new Page<>(tagPage.getCurrent(), tagPage.getSize(), tagPage.getTotal());
        resultPage.setRecords(adminVOList);
        return resultPage;
    }

    @Override
    public boolean removeById(Serializable id) {
        log.info("开始校验标签删除, id: {}", id);
        // 检查是否有文章关联此标签
        Long count = blogTagMapper.selectCount(new LambdaQueryWrapper<BlogTag>()
                .eq(BlogTag::getTagId, id));
        log.info("标签关联文章数: {}", count);
        if (count != null && count > 0) {
            throw new BusinessException(BlogErrorEnum.TAG_HAS_BLOGS);
        }
        return super.removeById(id);
    }

    @Override
    public void updateTagWithSync(Long id, String name) {
        log.info("开始更新标签, id: {}, name: {}", id, name);
        
        // 获取旧标签名称，用于判断是否需要更新ES和Blog表
        Tag oldTag = this.getById(id);
        String oldTagName = oldTag != null ? oldTag.getName() : null;
        
        // 更新数据库
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        this.updateById(tag);
        
        // 如果标签名称发生变化，需要同步更新ES索引和Blog表
        if (oldTagName != null && !oldTagName.equals(name)) {
            // 1. 更新 ES 索引
            try {
                Map<String, String> request = new HashMap<>();
                request.put("oldTagName", oldTagName);
                request.put("newTagName", name);
                searchServiceClient.updateTagName(request);
                log.info("同步更新ES标签名称成功, tagId={}, oldName={}, newName={}", 
                        id, oldTagName, name);
            } catch (Exception e) {
                log.error("同步更新ES标签名称失败, tagId={}", id, e);
                // 不抛出异常，避免影响主流程
            }
            
            // 2. 更新 Blog 表中的 tags 字段
            try {
                updateBlogTags(oldTagName, name);
                log.info("同步更新Blog表标签名称成功, tagId={}, oldName={}, newName={}", 
                        id, oldTagName, name);
            } catch (Exception e) {
                log.error("同步更新Blog表标签名称失败, tagId={}", id, e);
                // 不抛出异常，避免影响主流程
            }
        }
        
        log.info("更新标签成功, id: {}", id);
    }

    /**
     * 更新所有包含旧标签名的博客的tags字段
     * @param oldTagName 旧标签名称
     * @param newTagName 新标签名称
     */
    private void updateBlogTags(String oldTagName, String newTagName) {
        log.info("开始更新Blog表中的标签名称: {} -> {}", oldTagName, newTagName);
        
        // 查询所有包含旧标签名的博客
        List<Blog> blogs = blogMapper.selectList(
                new LambdaQueryWrapper<Blog>()
                        .like(Blog::getTags, oldTagName)
        );
        
        if (blogs == null || blogs.isEmpty()) {
            log.info("未找到需要更新标签名称的博客");
            return;
        }
        
        log.info("找到 {} 篇博客需要更新标签名称", blogs.size());
        
        int updatedCount = 0;
        // 批量更新博客的tags字段
        for (Blog blog : blogs) {
            String tags = blog.getTags();
            log.debug("处理博客 ID={}, 当前tags={}", blog.getId(), tags);
            
            if (StringUtils.hasText(tags)) {
                // 分割标签，精确替换
                String[] tagArray = tags.split(",");
                StringBuilder newTags = new StringBuilder();
                boolean needUpdate = false;
                
                for (int i = 0; i < tagArray.length; i++) {
                    String tag = tagArray[i].trim();
                    if (tag.equals(oldTagName)) {
                        newTags.append(newTagName);
                        needUpdate = true;
                        log.debug("博客 ID={} 中找到匹配标签，位置 {}", blog.getId(), i);
                    } else {
                        newTags.append(tag);
                    }
                    if (i < tagArray.length - 1) {
                        newTags.append(",");
                    }
                }
                
                // 只有当确实需要更新时才执行更新操作
                if (needUpdate) {
                    String oldTagsValue = blog.getTags();
                    blog.setTags(newTags.toString());
                    int rows = blogMapper.updateById(blog);
                    updatedCount++;
                    log.info("更新博客 ID={} 的tags字段: {} -> {}, 影响行数={}", 
                            blog.getId(), oldTagsValue, newTags.toString(), rows);
                } else {
                    log.debug("博客 ID={} 不需要更新（LIKE匹配但精确匹配失败）", blog.getId());
                }
            }
        }
        
        log.info("批量更新Blog表标签名称完成，共查询 {} 篇博客，实际更新 {} 篇", blogs.size(), updatedCount);
    }
}
