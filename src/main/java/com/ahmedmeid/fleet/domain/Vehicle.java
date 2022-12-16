package com.ahmedmeid.fleet.domain;

import java.io.Serializable;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Vehicle.
 */
@Table("vehicle")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("vehicle_id")
    private String vehicleId;

    @NotNull(message = "must not be null")
    @Column("vehicle_reg_no")
    private String vehicleRegNo;

    @Transient
    private Customer owner;

    @Column("owner_id")
    private Long ownerId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Vehicle id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleId() {
        return this.vehicleId;
    }

    public Vehicle vehicleId(String vehicleId) {
        this.setVehicleId(vehicleId);
        return this;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleRegNo() {
        return this.vehicleRegNo;
    }

    public Vehicle vehicleRegNo(String vehicleRegNo) {
        this.setVehicleRegNo(vehicleRegNo);
        return this;
    }

    public void setVehicleRegNo(String vehicleRegNo) {
        this.vehicleRegNo = vehicleRegNo;
    }

    public Customer getOwner() {
        return this.owner;
    }

    public void setOwner(Customer customer) {
        this.owner = customer;
        this.ownerId = customer != null ? customer.getId() : null;
    }

    public Vehicle owner(Customer customer) {
        this.setOwner(customer);
        return this;
    }

    public Long getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(Long customer) {
        this.ownerId = customer;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vehicle)) {
            return false;
        }
        return id != null && id.equals(((Vehicle) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Vehicle{" +
            "id=" + getId() +
            ", vehicleId='" + getVehicleId() + "'" +
            ", vehicleRegNo='" + getVehicleRegNo() + "'" +
            "}";
    }
}
