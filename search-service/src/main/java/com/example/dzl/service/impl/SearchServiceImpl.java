package com.example.dzl.service.impl;


import com.example.dzl.enums.SearchErrorEnum;
import com.example.dzl.exception.BusinessException;
import com.example.dzl.model.document.BlogDocument;
import com.example.dzl.model.dto.BlogIndexDTO;
import com.example.dzl.model.req.BlogSearchReq;
import com.example.dzl.model.vo.BlogSearchVO;
import com.example.dzl.model.vo.PageResult;
import com.example.dzl.repository.BlogDocumentRepository;
import com.example.dzl.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchServiceImpl implements SearchService {

    @Autowired
    private BlogDocumentRepository blogDocumentRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public void indexBlog(BlogIndexDTO dto) {
        log.info("开始建立索引博客: {}", dto);
        try {
            BlogDocument document = new BlogDocument();
            document.setId(dto.getId());
            document.setTitle(dto.getTitle());
            document.setContent(dto.getContent());
            document.setSummary(dto.getSummary());
            document.setAuthorId(dto.getAuthorId());
            document.setAuthorName(dto.getAuthorName());
            document.setAuthorAvatar(dto.getAuthorAvatar());
            document.setCategoryId(dto.getCategoryId());
            document.setCategoryName(dto.getCategoryName());
            document.setTags(dto.getTags());
            document.setStatus(dto.getStatus());
            document.setCoverImage(dto.getCoverImage());
            document.setViewCount(dto.getViewCount());
            document.setLikeCount(dto.getLikeCount());
            document.setCreateTime(dto.getCreateTime());
            document.setUpdateTime(dto.getUpdateTime());
            blogDocumentRepository.save(document);
            log.info("索引博客成功！");
        } catch (Exception e) {
            log.error("索引博客失败", e);
            throw new BusinessException(SearchErrorEnum.INDEX_FAILED);
        }
    }

    @Override
    public void deleteBlog(Long id) {
        try {
            blogDocumentRepository.deleteById(id);
        } catch (Exception e) {
            log.error("删除索引失败, id={}", id, e);
            throw new BusinessException(SearchErrorEnum.DELETE_FAILED);
        }
    }

    @Override
    public PageResult<BlogSearchVO> search(BlogSearchReq req) {
        int pageNum = req.getPageNum() != null && req.getPageNum() > 0 ? req.getPageNum() : 1;
        int pageSize = req.getPageSize() != null && req.getPageSize() > 0 ? req.getPageSize() : 10;
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StringUtils.hasText(req.getKeyword())) {
            MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(req.getKeyword())
                    .field("title", 3.0f)
                    .field("summary", 2.0f)
//                    .field("tags")
//                    .field("categoryName")
//                    .field("authorName")
                    ;
            boolQuery.must(multiMatchQuery);
        } else {
            boolQuery.must(QueryBuilders.matchAllQuery());
        }

        if (req.getAuthorId() != null) {
            boolQuery.filter(QueryBuilders.termQuery("authorId", req.getAuthorId()));
        }
        if (req.getCategoryId() != null) {
            boolQuery.filter(QueryBuilders.termQuery("categoryId", req.getCategoryId()));
        }
        boolQuery.filter(QueryBuilders.termQuery("status", 1));

        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .requireFieldMatch(false)
                .field("title")
                .field("summary")
                .preTags("<em>")
                .postTags("</em>");

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withHighlightBuilder(highlightBuilder)
                .withPageable(pageable);

        if (StringUtils.hasText(req.getSortBy()) && !"relevance".equalsIgnoreCase(req.getSortBy())) {
            String sortBy = req.getSortBy();
            String sortField = null;
            if ("time".equalsIgnoreCase(sortBy)) {
                sortField = "createTime";
            } else if ("viewCount".equalsIgnoreCase(sortBy)) {
                sortField = "viewCount";
            } else if ("likeCount".equalsIgnoreCase(sortBy)) {
                sortField = "likeCount";
            }
            if (sortField != null) {
                org.springframework.data.domain.Sort.Direction direction = "asc".equalsIgnoreCase(req.getSortOrder()) ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC;
                queryBuilder.withSort( org.springframework.data.domain.Sort.by(direction, sortField));
            }
        }

        NativeSearchQuery query = queryBuilder.build();

        SearchHits<BlogDocument> hits = elasticsearchRestTemplate.search(query, BlogDocument.class);

        List<BlogSearchVO> records = hits.getSearchHits().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<BlogSearchVO> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(hits.getTotalHits());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public void updateAuthorProfile(Long authorId, String authorName, String authorAvatar) {
        try {
            List<BlogDocument> documents = blogDocumentRepository.findByAuthorId(authorId);
            if (documents == null || documents.isEmpty()) {
                return;
            }
            for (BlogDocument document : documents) {
                if (authorName != null) {
                    document.setAuthorName(authorName);
                }
                if (authorAvatar != null) {
                    document.setAuthorAvatar(authorAvatar);
                }
            }
            blogDocumentRepository.saveAll(documents);
        } catch (Exception e) {
            log.error("根据作者更新索引失败, authorId={}", authorId, e);
            throw new BusinessException(SearchErrorEnum.INDEX_FAILED);
        }
    }

    @Override
    public void updateCategoryName(Long categoryId, String categoryName) {
        try {
            List<BlogDocument> documents = blogDocumentRepository.findByCategoryId(categoryId);
            if (documents == null || documents.isEmpty()) {
                log.info("未找到需要更新分类名称的博客文档, categoryId={}", categoryId);
                return;
            }
            for (BlogDocument document : documents) {
                document.setCategoryName(categoryName);
            }
            blogDocumentRepository.saveAll(documents);
            log.info("批量更新分类名称成功, categoryId={}, categoryName={}, 影响文档数={}", 
                    categoryId, categoryName, documents.size());
        } catch (Exception e) {
            log.error("批量更新分类名称失败, categoryId={}", categoryId, e);
            throw new BusinessException(SearchErrorEnum.INDEX_FAILED);
        }
    }

    @Override
    public void updateTagName(String oldTagName, String newTagName) {
        try {
            // 由于tags字段是逗号分隔的字符串，需要使用原生查询来匹配包含旧标签名的文档
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                    .must(QueryBuilders.wildcardQuery("tags", "*" + oldTagName + "*"));
            
            NativeSearchQuery query = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withPageable(PageRequest.of(0, 1000)) // 每次处理1000条
                    .build();

            SearchHits<BlogDocument> hits = elasticsearchRestTemplate.search(query, BlogDocument.class);
            if (hits.getTotalHits() == 0) {
                log.info("未找到需要更新标签名称的博客文档, oldTagName={}", oldTagName);
                return;
            }

            List<BlogDocument> documents = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            for (BlogDocument document : documents) {
                String tags = document.getTags();
                if (tags != null && tags.contains(oldTagName)) {
                    // 替换标签名称，确保精确匹配（避免部分匹配）
                    String[] tagArray = tags.split(",");
                    StringBuilder newTags = new StringBuilder();
                    for (int i = 0; i < tagArray.length; i++) {
                        String tag = tagArray[i].trim();
                        if (tag.equals(oldTagName)) {
                            newTags.append(newTagName);
                        } else {
                            newTags.append(tag);
                        }
                        if (i < tagArray.length - 1) {
                            newTags.append(",");
                        }
                    }
                    document.setTags(newTags.toString());
                }
            }
            blogDocumentRepository.saveAll(documents);
            log.info("批量更新标签名称成功, oldTagName={}, newTagName={}, 影响文档数={}", 
                    oldTagName, newTagName, documents.size());
        } catch (Exception e) {
            log.error("批量更新标签名称失败, oldTagName={}", oldTagName, e);
            throw new BusinessException(SearchErrorEnum.INDEX_FAILED);
        }
    }

    private BlogSearchVO convertToVO(SearchHit<BlogDocument> hit) {
        BlogDocument document = hit.getContent();
        Map<String, List<String>> highlightFields = hit.getHighlightFields();
        BlogSearchVO vo = new BlogSearchVO();
        vo.setId(document.getId());
        String title = document.getTitle();
        List<String> titleHighlight = highlightFields.get("title");
        if (titleHighlight != null && !titleHighlight.isEmpty()) {
            title = titleHighlight.get(0);
        }
        String summary = document.getSummary();
        List<String> summaryHighlight = highlightFields.get("summary");
        if (summaryHighlight != null && !summaryHighlight.isEmpty()) {
            summary = summaryHighlight.get(0);
        }
        vo.setTitle(title);
        vo.setSummary(summary);
        vo.setAuthorName(document.getAuthorName());
        vo.setAuthorAvatar(document.getAuthorAvatar());
        vo.setCategoryName(document.getCategoryName());
        vo.setTags(document.getTags());
        vo.setCoverImage(document.getCoverImage());
        vo.setViewCount(document.getViewCount());
        vo.setLikeCount(document.getLikeCount());
        vo.setCreateTime(document.getCreateTime());
        return vo;
    }
}


