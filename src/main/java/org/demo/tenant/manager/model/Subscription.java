package org.demo.tenant.manager.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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
    @Column(name = "current_subscription_url")
    private String currentSubscriptionUrl;
    private String url;
    private String status;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;
}
