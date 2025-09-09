package com.teamproject.sellog.common.emailUtils;

import java.io.IOException;
import java.util.Base64;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;

    }

    @Async
    public void sendHtmlEmail(EmailSendDto mailHtmlSendDto) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("subject", mailHtmlSendDto.getSubject());
            context.setVariable("message", mailHtmlSendDto.getContent());
            // if (mailHtmlSendDto.getTarget().equals("user")) {
            // context.setVariable("userType", "일반 사용자");
            // } else if (mailHtmlSendDto.getTarget().equals("admin")) {
            // context.setVariable("userType", "관리자");
            // }
            String base64Image = getBase64EncodedImage("static/images/logo.png");
            context.setVariable("logoImage", base64Image);

            String htmlContent = templateEngine.process("email-template", context);
            helper.setTo(mailHtmlSendDto.getEmailAddr());
            helper.setSubject(mailHtmlSendDto.getSubject());
            helper.setText(htmlContent, true);
            javaMailSender.send(message);
            System.out.println("Thymeleaf 템플릿 이메일 전송 성공!");
        } catch (MessagingException e) {
            System.out.println("[-] Thymeleaf 템플릿 이메일 전송 중 오류 발생: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 이미지를 Base64로 인코딩하는 메서드
    private String getBase64EncodedImage(String imagePath) throws IOException {
        Resource resource = new ClassPathResource(imagePath);
        byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());
        return Base64.getEncoder().encodeToString(bytes);
    }
}