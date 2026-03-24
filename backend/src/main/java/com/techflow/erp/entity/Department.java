package com.techflow.erp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // Managerul departamentului este tot un angajat
    @OneToOne
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JoinColumn(name = "manager_id")
    @JsonManagedReference
    private Employee manager;

    // Listă de angajați din acest departament
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @OneToMany(mappedBy = "department", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Employee> employees;
}