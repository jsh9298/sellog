package com.teamproject.sellog.auth.model.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 회원 탈퇴 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDeletDto {

    private String userId;
    private String password;

}
