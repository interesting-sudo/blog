package com.example.dzl.service;

public interface MailService {
    /**
     * 发送邮件
     * @param to 接收者
     * @param subject 主题
     * @param content 内容
     */
    void sendMail(String to, String subject, String content);

    /**
     * 发送HTML邮件
     * @param to 接收者
     * @param subject 主题
     * @param content HTML内容
     */
    void sendHtmlMail(String to, String subject, String content);
}