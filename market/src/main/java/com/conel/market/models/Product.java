package com.conel.market.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@SQLDelete(sql="UPDATE product SET deleted = true where id=?")
@SQLRestriction("deleted=false")//makes product disappear in website
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)

public class Product extends BaseEntity{
    @Column(nullable = false)
    private String name;
    private String description;
    private Double price;
    private Integer stockQuantity;

    private String imageUrl;
    @Column(nullable = false,columnDefinition = "boolean default false")
    private boolean deleted=false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;


    @OneToMany(mappedBy = "product")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrderItem> orderItems;
}
