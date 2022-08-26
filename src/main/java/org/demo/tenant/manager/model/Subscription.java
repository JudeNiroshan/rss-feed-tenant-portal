package org.demo.tenant.manager.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Getter
@Setter
@Entity
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "tenant_name")
    private String tenantName;
    @Column(name = "service_level")
    private String serviceLevel;
    private String url;
    private String status;

    @CreatedDate
    private Instant createdDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="tenant_id", nullable=false)
    private Tenant tenant;

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }
}
