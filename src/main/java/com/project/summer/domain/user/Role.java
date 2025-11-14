package com.project.summer.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    MASTER("ROLE_MASTER", "본사"),
    STORE("ROLE_STORE", "지점");

    private final String key;
    private final String title;
}
