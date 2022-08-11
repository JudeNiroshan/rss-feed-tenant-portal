package org.demo.tenant.manager.service;

import org.demo.tenant.manager.model.User;
import org.demo.tenant.manager.model.Subscription;
import org.demo.tenant.manager.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription save(String tenantName,
                             String serviceLevel,
                             String currentSubscriptionUrl,
                             User user) {

        Subscription subscription = new Subscription();
        subscription.setTenantName(tenantName);
        subscription.setServiceLevel(serviceLevel);
        subscription.setCurrentSubscriptionUrl(currentSubscriptionUrl);
        subscription.setUser(user);

        return subscriptionRepository.save(subscription);
    }
}
