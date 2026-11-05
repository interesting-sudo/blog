package com.example.dzl.service.impl;

import com.example.dzl.enums.UserErrorEnum;
import com.example.dzl.exception.BusinessException;
import com.example.dzl.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@Slf4j
public class MailServiceImpl implements MailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring..mail.username}")
    private String from;


    @Override
    public void sendMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        // 抄送给自己一份，以便在邮箱中查看记录
        message.setBcc(from);
        message.setSubject(subject);
        message.setText(content);
        try{
            mailSender.send(message);
            log.info("邮件发送成功，发送给：{}",to);
        }catch (Exception e){
            log.error("邮件发送失败，发送给：{}",to,e);
            throw new BusinessException(UserErrorEnum.EMAIL_SEND_FAIL);
        }
    }

    @Override
    public void sendHtmlMail(String to, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            // true表示需要创建一个multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            // 抄送给自己一份，以便在邮箱中查看记录
            helper.setBcc(from);
            helper.setSubject(subject);
            // true表示这是一个html
            helper.setText(content, true);

            mailSender.send(message);
            log.info("HTML邮件发送成功，发送给：{}", to);
        } catch (MessagingException e) {
            log.error("HTML邮件发送失败，发送给：{}", to, e);
            throw new BusinessException(UserErrorEnum.EMAIL_SEND_FAIL);
        }
    }
}
