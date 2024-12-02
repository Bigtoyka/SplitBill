package com.app.splitbill.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_participant")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BillParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bill_Item_id", nullable = false)
    private BillItem billItem;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser;

    @Column(name = "amount_owed", nullable = false)
    private BigDecimal amountOwed;

    @Column(name = "amount_paid", nullable = false)
    private BigDecimal amountPaid;
}
