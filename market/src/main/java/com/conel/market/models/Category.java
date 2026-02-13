package com.conel.market.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Category extends BaseEntity{

    @Column(unique = true,nullable = false,length = 50)
    private String name;

    private String description;

    @OneToMany(mappedBy = "category")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Product> products=new ArrayList<>();
}
