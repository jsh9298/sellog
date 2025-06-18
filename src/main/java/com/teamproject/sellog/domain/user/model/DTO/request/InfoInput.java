package com.teamproject.sellog.domain.user.model.DTO.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfoInput {
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