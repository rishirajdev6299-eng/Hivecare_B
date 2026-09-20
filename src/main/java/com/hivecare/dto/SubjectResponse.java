
package com.hivecare.dto;

public class SubjectResponse {

    private Long id;
    private String name;
    private Double price;
    //private String serviceName;

    public SubjectResponse() {
    }

    public SubjectResponse(Long id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
//        this.serviceName=serviceName;
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

//    public String getServiceName() {
//    	return serviceName; }
    // =====================================================
    // SETTERS
    // =====================================================

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
    
//    public void setServiceName(String serviceName) { 
//    	this.serviceName = serviceName; }
}

