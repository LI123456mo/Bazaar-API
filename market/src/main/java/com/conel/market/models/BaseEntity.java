package com.conel.market.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
@SoftDelete(columnName = "deleted")
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "base_seq")
    @SequenceGenerator(name = "base_seq",sequenceName = "base_sequence",allocationSize = 1)
    private Integer id;

    @CreatedDate
    @Column(updatable = false,nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private  LocalDateTime lastModifiedAt;

    @CreatedBy
    @Column(nullable = true,insertable = true)
    private String createdBy;

    @LastModifiedBy
    @Column(insertable = true)
    private String lastModifiedBy;
}
