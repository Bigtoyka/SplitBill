package com.app.splitbill.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillRequestDto {
    private BigDecimal totalAmount;
    private String description;
    private LocalDate date;
    private String createdBy;
    private String mainPayer;
    private String appGroup;
}
