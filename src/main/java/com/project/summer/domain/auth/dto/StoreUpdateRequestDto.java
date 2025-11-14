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
public class StoreUpdateRequestDto {

    @NotBlank(message = "매장 코드는 필수 입력 항목입니다.")
    private String storeCode;

    @NotBlank(message = "매장 이름은 필수 입력 항목입니다.")
    private String storeName;

    // 비밀번호는 변경안하면 기존 값 유지
    private String password;
}
