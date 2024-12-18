package com.app.splitbill.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DebtDto {
    private String debtorName;
    private String creditorName;
    private BigDecimal amount;
}
