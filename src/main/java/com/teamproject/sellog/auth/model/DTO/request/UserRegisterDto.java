package com.teamproject.sellog.auth.model.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDto {
    private String name;
    private String nickname;
    private String userId;
    private String password;
    private String email;
    private String address;
}