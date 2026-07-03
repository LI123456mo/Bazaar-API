package com.conel.market.entity.category;

import com.conel.market.entity.BaseEntity;
import com.conel.market.entity.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Category extends BaseEntity {

    @Column(unique = true,nullable = false,length = 50)
    private String name;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;
    private boolean active = true;

    @OneToMany(mappedBy = "category")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Product> products=new ArrayList<>();
}
