package com.teamproject.sellog.common.emailUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailSendDto {

    private String emailAddr; // 수신대상

    private String subject; // 이메일 제목

    private String content; // 이메일 내용
}