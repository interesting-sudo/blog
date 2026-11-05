package com.example.dzl.service.impl;

import com.example.dzl.enums.AiErrorEnum;
import com.example.dzl.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    private static final long AI_TIMEOUT_MILLIS = 10000;
    private static final int MAX_REQUESTS_PER_SECOND = 10;

    @Autowired
    private ChatClient chatClient;

    private long currentWindowStart = System.currentTimeMillis();
    private int currentWindowCount = 0;

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
    @Override
    public String processSelectedContent(String selectedContent, String userInstruction, String contextBefore, String contextAfter) {
        if (!StringUtils.hasText(selectedContent)) {
            return "";
        }
        if (!StringUtils.hasText(userInstruction)) {
            userInstruction = "润色优化";
        }
        log.info("处理选中内容，指令：{}，选中内容：{}", userInstruction, selectedContent);
        String prompt = buildSelectedContentPrompt(selectedContent, userInstruction, contextBefore, contextAfter);
        return callModelWithGuard(prompt);
    }

    @Override
    public Flux<String> streamProcessSelectedContent(String selectedContent, String userInstruction, String contextBefore, String contextAfter) {
        if (!StringUtils.hasText(selectedContent)) {
            return Flux.empty();
        }
        if (!tryAcquire()) {
            return Flux.just(AiErrorEnum.AI_RATE_LIMITED.getMsg());
        }

        if (!StringUtils.hasText(userInstruction)) {
            // 若用户未指定指令，则默认润色扩写
            userInstruction = "润色扩写";
        }
        log.info("流式处理选中内容，指令：{}，选中内容：{}", userInstruction, selectedContent);
        String prompt = buildSelectedContentPrompt(selectedContent, userInstruction, contextBefore, contextAfter);
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }

    /**
     * 构建选中内容处理的提示词
     */
    private String buildSelectedContentPrompt(String selectedContent, String userInstruction, String contextBefore, String contextAfter) {
        int selectedLength = selectedContent.trim().length();
        // 根据选中字数动态计算最低输出字数要求
        int minOutputLength;
        if (selectedLength < 30) {
            minOutputLength = 80;
        } else if (selectedLength < 100) {
            minOutputLength = 150;
        } else if (selectedLength < 300) {
            minOutputLength = selectedLength;
        } else {
            minOutputLength = (int) (selectedLength * 0.8);
        }
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("""
                你是一名专业的中文写作助手，用户正在编辑博客/文章，并选中了一段内容请求你处理。
                
                【用户指令】
                %s
                
                【核心要求】
                1. 严格按照用户指令处理选中的内容，支持以下两种操作：
                   - 润色扩写：在保留原文核心含义与情感基调的前提下，改善表达、丰富细节、扩展内容深度，让文字更流畅精彩、更有画面感和情绪温度，使内容更加饱满完整。
                   - 概念解释：针对选中的概念、术语或知识点，精准、简洁地补充完整其定义、背景与核心原理，内容言简意赅，不过度堆砌。
                2. 【字数要求】
                   - 若用户指令为「润色扩写」：输出内容字数不得少于 %d 个中文字符（含标点），选中原文字数越多输出应越丰富；选中内容较少时也必须达到最低字数，通过细节描写、场景补充、情绪延伸等方式自然扩充，不得为凑字数生硬堆砌。
                   - 若用户指令为「概念解释」：无字数下限，内容言简意赅即可，不得过度堆砌。
                3. 如果提供了上下文，请确保处理结果能与上下文自然衔接。
                4. 保持纯文本格式，不输出HTML标签或Markdown语法。
                5. 只输出处理后的内容，不要输出任何说明性文字、前缀或后缀。
                
                """.formatted(userInstruction, minOutputLength));

        // 添加上文（如果有）
        if (StringUtils.hasText(contextBefore)) {
            promptBuilder.append("""
                    【上文参考（不需修改，仅供理解语境）】
                    %s
                    
                    """.formatted(contextBefore));
        }

        // 添加选中内容
        promptBuilder.append("""
                【用户选中的内容（需要处理）】
                <<<
                %s
                >>>
                
                """.formatted(selectedContent));

        // 添加下文（如果有）
        if (StringUtils.hasText(contextAfter)) {
            promptBuilder.append("""
                    【下文参考（不需修改，仅供理解语境）】
                    %s
                    
                    """.formatted(contextAfter));
        }

        promptBuilder.append("请直接输出处理后的内容：");
        return promptBuilder.toString();
    }

    @Override
    public String generateSummary(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String prompt = """
                    你是专业中文技术编辑。任务：为博客正文生成一段「真实、简洁」的中文摘要。
                    写作规则（在脑中执行，不要输出步骤）：
                    1) 只基于正文内容本身进行概括，禁止无中生有、禁止编造不存在的观点或细节。
                    2) 正文很短（例如少于30个字）时，只允许生成一句极简摘要，长度不要明显多于原文。
                    3) 正文较长时，可以略微压缩，抓住主题和2~3个关键信息点。
                    硬性要求：
                    1) 仅输出摘要正文，不要标题/编号/Markdown/前后说明。
                    2) 总长度 ≤ 200 个中文字符（含标点），严禁为了凑长度而扩写。
                    3) 语言连贯，句子必须完整收束，不得出现半句截断。
                    4) 如果你判断正文内容质量很差、语义混乱，或字数太少以至于无法提炼出有意义的摘要，
                       请不要尝试编造摘要，而是只输出：NO_VALID_SUMMARY（全部大写，不要输出其他任何文字。
                    正文：
                    <<<
                    %s
                    >>>
                    """.formatted(content);
        return callModelWithGuard(prompt);
    }

    @Override
    public Flux<String> streamSummary(String content) {
        if (!StringUtils.hasText(content)) {
            return Flux.empty();
        }
        if (!tryAcquire()) {
            return Flux.just(AiErrorEnum.AI_RATE_LIMITED.getMsg());
        }
        String prompt = """
                    你是专业中文技术编辑。任务：为博客正文生成一段「真实、简洁」的中文摘要，并以流式方式输出。

                    写作规则（在脑中执行，不要输出步骤）：
                    1) 只基于正文内容本身进行概括，禁止无中生有、禁止编造不存在的观点或细节。
                    2) 正文很短（例如少于30个字）时，只允许生成一句极简摘要，长度不要明显多于原文。
                    3) 正文较长时，可以略微压缩，抓住主题和2~3个关键信息点。

                    硬性要求：
                    1) 仅输出摘要正文，不要标题/编号/Markdown/前后说明。
                    2) 总长度 ≤ 200 个中文字符（含标点），严禁为了凑长度而扩写。
                    3) 语言连贯，句子必须完整收束，不得出现半句截断。
                    4) 如果你判断正文内容质量很差、语义混乱，或字数太少以至于无法提炼出有意义的摘要，
                       请不要尝试编造摘要，而是只输出这句中文提示：
                       “正文质量太差或过短，无法生成摘要，请先整理内容。”

                    正文：
                    <<<
                    %s
                    >>>
                    """.formatted(content);
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }

    @Override
    public String generateTitle(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String prompt = """
                    你是严苛的中文博客主编。请根据正文生成一个**极简**标题。
                    
                    【智能风格切换】
                    - 技术类：核心技术栈+解决的问题/结论（去掉“浅谈/基于/实战”等废话）。
                    - 生活类：核心事件/话题+情感/亮点（去掉“记一次/关于”等废话）。
                    
                    【字数红线 - 必须严格执行】
                    1. **长度限制**：标题必须控制在 **20 个字以内**（最长绝对不超过 25 字）。
                    2. **压缩指令**：如果标题超过 25 字，**必须**删减形容词和连接词，直到满足要求。如果你生成后发现超过25字：必须删减改写，直到≤25字为止；宁可更短也不能超长。
                    3. **禁止套话**：严禁使用“本文将介绍”、“关于...的思考”、“浅析”、“深入理解”等凑字数前缀。
                    4. **输出格式**：只返回标题正文，不要引号，不要解释。
                    5. 【重要】如果你认为正文质量太差、严重胡言乱语或无法理解，导致无法生成有意义的标题，请不要生成标题，直接只输出：NO_VALID_TITLE（全部大写，不要输出其他任何文字）。
                    
                    【学习样例】
                    [❌] 错误（太长）：深入浅出带你了解关于Spring Boot 3.0的核心特性与实战应用 (29字)
                    [✅] 正确（压缩）：Spring Boot 3.0 核心特性与实战 (14字)
                    
                    [❌] 错误（太长）：记一次我在云南大理旅游过程中发生的那些有趣感人的故事 (26字)
                    [✅] 正确（压缩）：云南大理游记：遇见最美的自己 (13字)
                    
                    【自检步骤（在脑中完成，不要输出）】
                    1) 生成标题；
                    2) 统计字符数（按上面规则）；
                    3) 若字数>25，继续压缩并复检，直到字数≤25
                    
                    
                    正文：
                    <<<
                    %s
                    >>>
                    """.formatted(content);
        return callModelWithGuard(prompt);
    }

    @Override
    public String generateTags(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String prompt = """
                        你是博客内容运营编辑。请从正文中提取标签，用于检索与分类。
                        
                        写作策略：
                        - 若正文是技术内容：标签优先选择“技术名词/框架/工具/核心概念/错误关键词/场景”（例如：Spring Boot,MySQL,索引优化,JWT,Docker）。
                        - 若正文是非技术内容：标签选择“主题/对象/地点/事件/情绪或观点”。
                        
                        硬性要求：
                        1) 只返回标签列表，用英文逗号分隔，例如：Spring Boot,MySQL,索引优化
                        2) 返回 3 到 5 个标签，按相关性从高到低排序。
                        3) 每个标签尽量短（2~10个字），不要重复。
                        4) 避免空泛标签：技术类禁用“技术/编程/学习/笔记/经验”；非技术类禁用“生活/随笔/记录/分享”。
                        5) 不要加#号，不要空格，不要换行，不要输出任何解释。
                        
                        正文：
                        <<<
                        %s
                        >>>
                        """.formatted(content);
        return callModelWithGuard(prompt);
    }

    @Override
    public boolean hasSensitiveWord(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        String prompt = "你是一个内容安全审核助手，请严格根据中国法律法规判断下面的评论内容中是否包含违法违规、辱骂、人身攻击、色情、暴恐、诈骗、政治敏感等不当内容。" +
                "如果包含，请只回复 YES；如果不包含，请只回复 NO。评论内容：\n\n" + content;
        String result = callModelWithGuard(prompt);
        if (!StringUtils.hasText(result)) {
            return false;
        }
        String normalized = result.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("Y");
    }

    /**
     * 尝试获取模型调用权限，基于滑动窗口速率限制
     * @return true表示成功获取，false表示速率限制拒绝
     */
    private synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        if (now - currentWindowStart >= 1000) {
            currentWindowStart = now;
            currentWindowCount = 0;
        }
        if (currentWindowCount >= MAX_REQUESTS_PER_SECOND) {
            return false;
        }
        currentWindowCount++;
        return true;
    }

    /**
     * 调用模型并添加速率限制保护
     * @param prompt 模型输入提示
     * @return 模型输出结果
     */
    private String callModelWithGuard(String prompt) {
        if (!tryAcquire()) {
            return AiErrorEnum.AI_RATE_LIMITED.getMsg();
        }
        try {

            return CompletableFuture.supplyAsync(() ->
                            chatClient.prompt()
                                    .user(prompt)
                                    .call()
                                    .content()
                    )
                    .get(AI_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return AiErrorEnum.AI_TIMEOUT.getMsg();
        } catch (Exception e) {
            return AiErrorEnum.AI_SERVICE_UNAVAILABLE.getMsg();
        }
    }
}
