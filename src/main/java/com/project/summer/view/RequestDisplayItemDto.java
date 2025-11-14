package com.project.summer.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDisplayItemDto {
    private String productCode;
    private String productName;
    private int quantity;
}
