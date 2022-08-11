package org.demo.tenant.manager.repository;

import org.demo.tenant.manager.model.Subscription;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {

    @Override
    List<Subscription> findAll();

}
