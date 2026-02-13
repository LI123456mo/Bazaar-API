package com.conel.market.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.SoftDelete;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Entity
public class Product extends BaseEntity{
    @Column(nullable = false)
    private String name;
    private String description;
    private Double price;
    private Integer stockQuantity;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;


    @OneToMany(mappedBy = "product")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrderItem> orderItems;
}
