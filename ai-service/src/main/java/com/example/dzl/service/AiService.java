package com.example.dzl.service;

import reactor.core.publisher.Flux;

public interface AiService {


    /**
     * 根据用户指令处理选中的内容（非流式）
     * 支持润色、扩写、解释概念、补充内容等多种操作
     *
     * @param selectedContent 用户选中的内容
     * @param userInstruction 用户的指令/意图描述（如：润色、扩写、解释这个概念等）
     * @param contextBefore   选中内容的上文（可为空）
     * @param contextAfter    选中内容的下文（可为空）
     * @return 处理后的内容
     */
    String processSelectedContent(String selectedContent, String userInstruction, String contextBefore, String contextAfter);

    /**
     * 根据用户指令处理选中的内容（流式）
     * 支持润色、扩写、解释概念、补充内容等多种操作
     *
     * @param selectedContent 用户选中的内容
     * @param userInstruction 用户的指令/意图描述
     * @param contextBefore   选中内容的上文（可为空）
     * @param contextAfter    选中内容的下文（可为空）
     * @return 处理结果的流式发布者
     */
    Flux<String> streamProcessSelectedContent(String selectedContent, String userInstruction, String contextBefore, String contextAfter);
    /**
     * 生成摘要
     * @param content 原始内容
     * @return 摘要内容
     */
    String generateSummary(String content);

    /**
     * 流式生成摘要
     * @param content 原始内容
     * @return 摘要的流式发布者
     */
    Flux<String> streamSummary(String content);
    /**
     * 对内容生成标题
     * @param content 原始内容
     * @return 标题内容
     */
    String generateTitle(String content);

    /**
     * 从内容中提取3到5个最合适的中文标签
     * @param content 原始内容
     * @return 逗号分隔的标签列表
     */
    String generateTags(String content);
    /**
     * 检查内容是否包含敏感词
     * @param content 原始内容
     * @return true表示包含敏感词，false表示不包含
     */
    boolean hasSensitiveWord(String content);
}
