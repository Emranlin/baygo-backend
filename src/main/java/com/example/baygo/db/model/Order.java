package com.example.baygo.db.model;

import com.example.baygo.db.model.enums.PaymentType;
import com.example.baygo.db.model.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static jakarta.persistence.CascadeType.*;

@Getter
@Setter
@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_gen")
    @SequenceGenerator(name = "order_gen", sequenceName = "order_seq", allocationSize = 1, initialValue = 6)
    private Long id;
    private LocalDate dateOfOrder;
    private LocalDate dateOfReceived;
    private BigDecimal totalPrice;
    @Enumerated(EnumType.STRING)
    private Status status;
    private boolean withDelivery;
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    private String orderNumber;

    @ManyToOne(cascade = {REFRESH, DETACH, MERGE, PERSIST})
    @JoinColumn(name = "buyer_id")
    private Buyer buyer;

    @ElementCollection
    @JoinTable(name = "orders_sub_products_size",
            joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyJoinColumn(name = "sub_products_size_id")
    @Cascade({CascadeType.REFRESH, CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST})
    private Map<Size, Integer> productCount;
    @OneToOne(cascade = ALL)
    private Customer customer;
}