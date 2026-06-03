package com.conel.market.models;

import com.conel.market.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "ROLES")
public class Role extends BaseEntity{

    @Column(name = "NAME",nullable = false)
    private String name;

    @ManyToMany(mappedBy = "roles")
    List<User> users=new ArrayList<>();
}
