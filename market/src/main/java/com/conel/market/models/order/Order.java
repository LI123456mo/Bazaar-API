package com.conel.market.models.order;

import com.conel.market.models.BaseEntity;
import com.conel.market.models.OrderItem;
import com.conel.market.models.OrderStatus;
import com.conel.market.models.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseEntity {

    @Column(nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private String paymentMethod; // e.g., "MPESA", "CASH_ON_DELIVERY"

    @Column(columnDefinition = "TEXT")
    private String shippingAddress; // Snapshot of the delivery destination text

    @ManyToOne(fetch = FetchType.LAZY) // Optimized lazy loading for order owner lookups
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrderItem> orderItems = new ArrayList<>();
}