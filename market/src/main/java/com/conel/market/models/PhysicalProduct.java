package com.conel.market.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class PhysicalProduct extends Product{

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Integer length;

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;
}
