package com.conel.market.user.entity;

import com.conel.market.entity.address.Address;
import com.conel.market.entity.order.Order;
import com.conel.market.entity.role.Role;
import com.conel.market.entity.product.Product;
import com.conel.market.entity.vendor.VendorStatus;
import com.conel.market.security.RoleEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import jakarta.annotation.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;

import static jakarta.persistence.GenerationType.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SQLRestriction("is_deleted=false")
@Table(name = "_user")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = UUID)
    private String id;

    private String firstName;

    private String lastName;


    private String email;

    private String phoneNumber;

    private String password;

    private LocalDate dateOfBirth;

    @Builder.Default
    private boolean enabled=true;

    @Builder.Default
    private boolean locked=false;

    @Builder.Default
    private boolean credentialsExpired=false;

    @Builder.Default
    private boolean emailVerified=false;

    @Builder.Default
    private boolean phoneVerified=false;

    @Builder.Default
    @Column(name = "is_deleted",nullable = false)
    private boolean deleted=false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private VendorStatus vendorStatus=VendorStatus.PENDING_APPROVAL;

    @Column(nullable = true)
    @Builder.Default
    private boolean vendorApproved=false;

    public void softDelete() {
        String timestamp = String.valueOf(System.currentTimeMillis());

        this.deleted = true;
        this.enabled = false;
        this.locked = true;

        this.firstName = "Deleted";
        this.lastName = "User";
        this.email = "deleted_" + this.id + "_" + timestamp + "@deleted.local";

        if (this.phoneNumber != null) {
            this.phoneNumber = "deleted_" + this.id + "_" + timestamp;
        }
    }


    @ManyToMany(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "USERS_ROLES",
            joinColumns = {
                    @JoinColumn(name = "users_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "roles_id")
            }
    )
    private List<Role> roles=new ArrayList<>();

    // Financial records (Orders) must NEVER be cascade-deleted
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Order> orderList = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Address> addresses;

    // Products shouldn't be deleted instantly; they should be unlisted via a flag in Product entity
    @OneToMany(mappedBy = "seller", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Product> products = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (CollectionUtils.isEmpty(this.roles)){
            return List.of();
        }
        Set<SimpleGrantedAuthority> authorities=new HashSet<>();

        //for backward compatibility
        this.roles.forEach(role->authorities.add(
                new SimpleGrantedAuthority("ROLE_"+role.getName().toUpperCase())));


        //fine-grained permissions
        this.roles.forEach(role -> {
            RoleEnum roleEnum=RoleEnum.valueOf(role.getName().toUpperCase());

            roleEnum.getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission.getPermission()))
            );
        });
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !deleted;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled && !deleted;
    }
}
