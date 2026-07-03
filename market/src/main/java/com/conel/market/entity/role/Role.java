package com.conel.market.entity.role;

import com.conel.market.entity.BaseEntity;
import com.conel.market.user.entity.User;
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
public class Role extends BaseEntity {

    @Column(name = "NAME",nullable = false)
    private String name;

    @ManyToMany(mappedBy = "roles")
    List<User> users=new ArrayList<>();
}
