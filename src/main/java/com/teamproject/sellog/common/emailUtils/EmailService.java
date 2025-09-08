package com.teamproject.sellog.common.emailUtils;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * 단순 텍스트 이메일 발송
     * 
     * @param to      수신자 이메일 주소
     * @param subject 이메일 제목
     * @param text    이메일 본문 (일반 텍스트)
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        // setFrom(): 반드시 Email Communication Services에 설정된 도메인의 유효한 발신자 주소 사용
        // 예: "donotreply@xxxx.azurecomm.net" 또는 "no-reply@yourdomain.com"
        message.setFrom("DoNotReply@c63138e7-6e8c-4ff6-a57f-a848d14d2b13.azurecomm.net");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
        System.out.println("이메일이 성공적으로 " + to + " (으)로 전송되었습니다.");
    }

    /**
     * HTML 이메일 발송 (인증/프로모션/알림 등에서 유용)
     * HTML 템플릿 사용을 위해 Thymeleaf, FreeMarker 등과 연동하여 사용 가능합니다.
     */
    // @Async // 비동기 발송을 위해 추가 (비동기 설정 필요)
    // public void sendHtmlEmail(String to, String subject, String htmlContent)
    // throws MessagingException {
    // MimeMessage message = javaMailSender.createMimeMessage();
    // MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    // helper.setFrom("발신자_이메일_주소");
    // helper.setTo(to);
    // helper.setSubject(subject);
    // helper.setText(htmlContent, true); // true = HTML content
    // javaMailSender.send(message);
    // System.out.println("HTML 이메일이 성공적으로 " + to + " (으)로 전송되었습니다.");
    // }
}