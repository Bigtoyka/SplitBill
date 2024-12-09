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
public class BillParticipantRequestDto {
    private String itemName;
    private String groupName;
    private String username;
    private BigDecimal amountOwed;
    private BigDecimal amountPaid;
}
