package com.teamproject.sellog.common.emailUtils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailSendDto {

    private String emailAddr; // 수신대상

    private String subject; // 이메일 제목

    private String content; // 이메일 내용

    public EmailSendDto(String emailAddr, String subject, String content) {
        this.emailAddr = emailAddr;
        this.subject = subject;
        this.content = content;
    }

}