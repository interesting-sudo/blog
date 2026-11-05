package com.example.dzl.repository;

import com.example.dzl.model.document.BlogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface BlogDocumentRepository extends ElasticsearchRepository<BlogDocument, Long> {
    List<BlogDocument> findByAuthorId(Long authorId);
    
    /**
     * 根据分类ID查询博客文档
     */
    List<BlogDocument> findByCategoryId(Long categoryId);
}
