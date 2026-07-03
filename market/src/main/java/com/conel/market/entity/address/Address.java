package com.conel.market.entity.address;

import com.conel.market.user.entity.User;
import com.conel.market.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@SuperBuilder
@Entity
public class Address extends BaseEntity {


    private String streetName;

    private String houseNumber;

    private String zipCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude//stop address from printing the whole user
    @EqualsAndHashCode.Exclude//stop address from comparing the whole user
    private User user;
}
