package com.app.splitbill.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payer_id")
    private AppUser payer;

    @ManyToOne
    @JoinColumn(name = "payee_id")
    private AppUser payee;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
}
