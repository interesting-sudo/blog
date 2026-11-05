# 表 X AI 内容处理流程伪代码

```
输入：content 待处理文本；streamFlag 是否流式；promptParams 构建 Prompt 的业务参数（可为空）
输出：result 模型处理结果

processAiRequest(content, streamFlag, promptParams)
 1  IF IS-EMPTY(content) THEN                                       // 空内容不进入 AI 流程
 2      IF streamFlag = true THEN                                    // 流式请求且无有效文本
 3          RETURN Flux.empty()                                       // 直接返回空流，不调用模型
 4      ENDIF
 5      RETURN emptyResult
 6  ENDIF
 7  acquired = tryAcquire()                                          // 限流校验：滑动窗口，每秒最多 10 次
 8  IF acquired = false THEN                                         // 超过阈值
 9      RETURN rateLimitMessage                                       // 返回限流提示
10  ENDIF
11  prompt = buildPrompt(content, promptParams)                       // 按业务参数渲染 Prompt 模板
12  IF streamFlag = true THEN                                        // 流式处理
13      RETURN chatClient.prompt().user(prompt).stream().content()    // Flux 持续返回字符流
14  ENDIF
15  TRY
16      future = CompletableFuture.supplyAsync(() ->                  // 异步提交模型调用，避免长时间阻塞
17          chatClient.prompt().user(prompt).call().content()         // 调用 ChatClient 获取完整文本
18      )
19      result = future.get(AI_TIMEOUT_MILLIS)                       // 等待模型生成，超时 10 秒
20  CATCH TimeoutException                                            // 调用超时
21      RETURN timeoutMessage
22  CATCH Exception                                                   // 模型或网络异常
23      RETURN serviceUnavailableMessage
24  ENDTRY
25  IF IS-EMPTY(result) THEN                                          // 模型未返回有效内容
26      RETURN serviceUnavailableMessage
27  ENDIF
28  RETURN result                                                     // 非流式：等待生成后一次性返回
```
