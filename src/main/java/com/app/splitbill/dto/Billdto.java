package com.app.splitbill.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
public class Billdto {
    private Long groupId;
    private BigDecimal totalAmount;
    private String description;
    private LocalDate date;
    private Long createdById;
    private Long mainPayerId;
}
