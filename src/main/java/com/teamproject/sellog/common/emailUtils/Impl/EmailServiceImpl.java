package com.teamproject.sellog.common.emailUtils.Impl;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.teamproject.sellog.common.emailUtils.EmailSendDto;
import com.teamproject.sellog.common.emailUtils.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public EmailServiceImpl(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;

    }

    @Async
    public void sendEmail(EmailSendDto mailHtmlSendDto) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("subject", mailHtmlSendDto.getSubject());
            context.setVariable("message", mailHtmlSendDto.getContent());

            String htmlContent = templateEngine.process("email-template", context);
            helper.setFrom(""); // 다른 이메일 주소로 변경할 필요 있음.
            helper.setTo(mailHtmlSendDto.getEmailAddr());
            helper.setSubject(mailHtmlSendDto.getSubject());
            helper.setText(htmlContent, true);
            javaMailSender.send(message);
            System.out.println("Thymeleaf 템플릿 이메일 전송 성공!");
        } catch (MessagingException e) {
            System.out.println("[-] Thymeleaf 템플릿 이메일 전송 중 오류 발생: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}