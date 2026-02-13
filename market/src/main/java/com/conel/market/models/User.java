package com.conel.market.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SoftDelete;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
public class User extends BaseEntity{
    private String firstName;
    private String lastName;
    @Column(unique = true,nullable = false)
    private String email;
    private String password;
    private String role;

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Order> orderList=new ArrayList<>();
}
