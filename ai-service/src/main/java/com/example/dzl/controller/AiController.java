package com.example.dzl.controller;

import com.example.dzl.enums.AiErrorEnum;
import com.example.dzl.service.AiService;
import com.example.dzl.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
@Slf4j
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * 生成博客标题
     * @param req 包含博客内容的请求体
     * @return 博客标题
     */
    @PostMapping("/title")
    public Result<String> title(@RequestBody ContentReq req) {
        log.info("ai开始生成标题");
        if (req == null || !StringUtils.hasText(req.getContent())) {
            return Result.error(AiErrorEnum.AI_INVALID_CONTENT.getCode(), AiErrorEnum.AI_INVALID_CONTENT.getMsg());
        }
        String title = aiService.generateTitle(req.getContent());
        if (!StringUtils.hasText(title)) {
            return Result.error(AiErrorEnum.AI_TITLE_CANNOT_GENERATE.getCode(), AiErrorEnum.AI_TITLE_CANNOT_GENERATE.getMsg());
        }
        String normalized = title.trim();
        if ("NO_VALID_TITLE".equals(normalized)) {
            return Result.error(AiErrorEnum.AI_TITLE_CANNOT_GENERATE.getCode(), AiErrorEnum.AI_TITLE_CANNOT_GENERATE.getMsg());
        }
        return Result.success(title);
    }


    /**
     * 生成博客摘要
     * @param req 包含博客内容的请求体
     * @return 博客摘要
     */
    @PostMapping("/summary")
    public Result<String> summary(@RequestBody ContentReq req) {
        if (req == null || !StringUtils.hasText(req.getContent())) {
            return Result.error(AiErrorEnum.AI_INVALID_CONTENT.getCode(), AiErrorEnum.AI_INVALID_CONTENT.getMsg());
        }
        String summary = aiService.generateSummary(req.getContent());
        if (!StringUtils.hasText(summary)) {
            return Result.error(AiErrorEnum.AI_SUMMARY_CANNOT_GENERATE.getCode(), AiErrorEnum.AI_SUMMARY_CANNOT_GENERATE.getMsg());
        }
        String normalized = summary.trim();
        if ("NO_VALID_SUMMARY".equals(normalized)) {
            return Result.error(AiErrorEnum.AI_SUMMARY_CANNOT_GENERATE.getCode(), AiErrorEnum.AI_SUMMARY_CANNOT_GENERATE.getMsg());
        }
        return Result.success(summary);
    }

    @PostMapping(value = "/summary/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> summaryStream(@RequestBody ContentReq req) {
        if (req == null || !StringUtils.hasText(req.getContent())) {
            return Flux.just("内容为空或无效，无法生成摘要，请先输入正文。");
        }
        return aiService.streamSummary(req.getContent())
                .collectList()
                .flatMapMany(chunks -> {
                    String all = String.join("", chunks).trim();
                    if ("NO_VALID_SUMMARY".equals(all)) {
                        return Flux.just("正文质量太差或过短，无法生成摘要，请先整理内容。");
                    }
                    return Flux.fromIterable(chunks);
                });
    }

    /**
     * 从内容中提取3到5个最合适的中文标签
     * @param req 包含博客内容的请求体
     * @return 逗号分隔的标签列表
     */
    @PostMapping("/tags")
    public Result<String> tags(@RequestBody ContentReq req) {
        return Result.success(aiService.generateTags(req.getContent()));
    }
    /**
     * 检查内容是否包含敏感词
     * @param req 包含博客内容的请求体
     * @return true表示包含敏感词，false表示不包含
     */
    @PostMapping("/sensitive")
    public Result<Boolean> sensitive(@RequestBody ContentReq req) {
        return Result.success(aiService.hasSensitiveWord(req.getContent()));
    }

    /**
     * 根据用户指令处理选中的内容（非流式）
     * 支持润色、扩写、解释概念、补充内容等多种操作
     * @param req 包含选中内容、用户指令、上下文的请求体
     * @return 处理后的内容
     */
    @PostMapping("/process")
    public Result<String> processSelectedContent(@RequestBody SelectedContentReq req) {
        log.info("AI开始处理选中内容，指令：{}", req.getUserInstruction());
        if (req == null || !StringUtils.hasText(req.getSelectedContent())) {
            return Result.error(AiErrorEnum.AI_INVALID_CONTENT.getCode(), AiErrorEnum.AI_INVALID_CONTENT.getMsg());
        }
        String result = aiService.processSelectedContent(
                req.getSelectedContent(),
                req.getUserInstruction(),
                req.getContextBefore(),
                req.getContextAfter()
        );
        if (!StringUtils.hasText(result)) {
            return Result.error(AiErrorEnum.AI_SERVICE_UNAVAILABLE.getCode(), AiErrorEnum.AI_SERVICE_UNAVAILABLE.getMsg());
        }
        return Result.success(result);
    }

    /**
     * 根据用户指令处理选中的内容（流式）
     * 支持润色、扩写、解释概念、补充内容等多种操作
     * @param req 包含选中内容、用户指令、上下文的请求体
     * @return 处理结果的流式发布者
     */
    @PostMapping(value = "/process/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> streamProcessSelectedContent(@RequestBody SelectedContentReq req, HttpServletResponse response) {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8");
        log.info("AI开始流式处理选中内容，指令：{}", req.getUserInstruction());
        if (req == null || !StringUtils.hasText(req.getSelectedContent())) {
            return Flux.just("选中内容为空，无法处理。");
        }
        return aiService.streamProcessSelectedContent(
                req.getSelectedContent(),
                req.getUserInstruction(),
                req.getContextBefore(),
                req.getContextAfter()
        );
    }



    @Data
    public static class ContentReq {
        private String content;
    }

    @Data
    public static class SelectedContentReq {
        /**
         * 用户选中的内容（必填）
         */
        private String selectedContent;
        /**
         * 用户的指令/意图描述，如：润色、扩写、解释这个概念等（默认“润色优化”）
         */
        private String userInstruction;
        /**
         * 选中内容的上文（可为空）
         */
        private String contextBefore;
        /**
         * 选中内容的下文（可为空）
         */
        private String contextAfter;
    }
}

