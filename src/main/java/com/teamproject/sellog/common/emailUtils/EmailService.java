package com.teamproject.sellog.common.emailUtils;

import org.springframework.stereotype.Service;

@Service
public interface EmailService {

    void sendEmail(EmailSendDto mailHtmlSendDto);
}