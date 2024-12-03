package com.app.splitbill.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DebtDto {
    private String debtorName;
    private BigDecimal amount;

    public DebtDto(String debtorName, BigDecimal amount) {
        this.debtorName = debtorName;
        this.amount = amount;
    }
}
