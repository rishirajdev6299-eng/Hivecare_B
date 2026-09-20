package com.hivecare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "subject")
public class Subject {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private String name;

private Double price;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "service_id", nullable = false)
private Service service;

public Subject() {
}

public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public String getName() {
    return name;
}

public void setName(String name) {
    this.name = name;
}

public Double getPrice() {
    return price;
}

public void setPrice(Double price) {
    this.price = price;
}

public Service getService() {
    return service;
}

public void setService(Service service) {
    this.service = service;
}


}
