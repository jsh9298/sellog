package com.teamproject.sellog.domain.user.model.DTO.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Response {
    private String nickname;
    private String profileMessage;
    private String profileThumbURL;
    private String profileURL;
    private String userName;
    private String userAddress;
    private String phoneNumber;
    private String email;
    private String password;
}