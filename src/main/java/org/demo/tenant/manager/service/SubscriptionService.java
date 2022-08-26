package org.demo.tenant.manager.service;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.NamespaceableResource;
import io.fabric8.openshift.client.OpenShiftClient;
import org.demo.tenant.manager.model.Subscription;
import org.demo.tenant.manager.model.Tenant;
import org.demo.tenant.manager.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription save(Tenant tenant) {

        Subscription subscription = new Subscription();
        subscription.setTenantName(tenant.getTenantUserName());
        subscription.setTenant(tenant);
        subscription.setStatus("Pending");
        subscription.setCreatedDate(Instant.now());

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        String namespaceName = savedSubscription.getTenantName() + System.currentTimeMillis();
        String freshRssName = savedSubscription.getTenantName();
        String crdApiVersion = "freshrss.demo.openshift.com/v1alpha1";
        String crdKind = "FreshRSS";
        String defaultUser = tenant.getTenantUserName();

        try (final KubernetesClient k8sClient = new KubernetesClientBuilder().build()) {
            // Do stuff with client
            OpenShiftClient ocpClient = k8sClient.adapt(OpenShiftClient.class);
            Namespace namespace = new NamespaceBuilder()
                    .withNewMetadata()
                    .withName(namespaceName)
                    .endMetadata()
                    .build();
            ocpClient.namespaces().createOrReplace(namespace);

            Map<String, Object> map = new HashMap<>();
            map.put("defaultUser", defaultUser);
            map.put("title", savedSubscription.getTenantName());

            GenericKubernetesResource crdResource = new GenericKubernetesResource();
            crdResource.setKind(crdKind);
            crdResource.setApiVersion(crdApiVersion);
            crdResource.setMetadata(new ObjectMetaBuilder()
                    .withNamespace(namespace.getMetadata().getName())
                    .withName(freshRssName)
                    .build());
            crdResource.setAdditionalProperty("spec", map);
            NamespaceableResource<GenericKubernetesResource> resource = ocpClient.resource(crdResource);
            resource.createOrReplace();

            List<String> urlList = new ArrayList<>();

            while (urlList.isEmpty()) {
                Thread.sleep(200);

                List<GenericKubernetesResource> list = ocpClient
                        .genericKubernetesResources("freshrss.demo.openshift.com/v1alpha1", crdKind)
                        .inNamespace(namespaceName)
                        .list().getItems();
                list.get(0).getAdditionalProperties().entrySet().stream()
                        .filter(e -> e.getKey().equals("status"))
                        .findFirst()
                        .ifPresent(e -> {
                            if (e.getValue() instanceof LinkedHashMap) {
                                LinkedHashMap linkedHashMap = (LinkedHashMap) e.getValue();
                                String url = (String) linkedHashMap.get("url");
                                urlList.add(url);
                            }
                        });
            }

            if (!urlList.isEmpty()) {
                System.out.println("URL --->" + urlList.get(0));
                savedSubscription.setUrl(urlList.get(0));
                savedSubscription.setStatus("Active");
            } else {
                System.out.println("URL couldn't fetch from the FreshRSS crd!!!");
            }

            ocpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Subscription save = subscriptionRepository.save(savedSubscription);
        return save;
    }
}
