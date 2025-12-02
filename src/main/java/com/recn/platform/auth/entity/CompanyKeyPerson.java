package com.recn.platform.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "company_key_people")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyKeyPerson {

    @Id
    @UuidGenerator
    @Column(name = "key_person_id", length = 36, nullable = false, updatable = false)
    private String keyPersonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;
}

