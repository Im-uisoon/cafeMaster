package com.project.summer.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = "매장 코드는 필수입니다")
    private String storeCode;

    @NotBlank(message = "매장 이름은 필수입니다")
    private String storeName;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
