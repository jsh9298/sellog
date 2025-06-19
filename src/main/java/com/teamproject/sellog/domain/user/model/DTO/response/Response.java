package com.teamproject.sellog.domain.user.model.DTO.response;

import lombok.Builder;
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

    @Builder
    public Response(String nickname, String profileMessage, String profileThumbURL, String profileURL, String userName,
            String userAddress, String phoneNumber, String email, String password) {
        this.nickname = nickname;
        this.profileMessage = profileMessage;
        this.profileThumbURL = profileThumbURL;
        this.profileURL = profileURL;
        this.userName = userName;
        this.userAddress = userAddress;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
    }

}