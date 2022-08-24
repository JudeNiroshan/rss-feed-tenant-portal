package org.demo.tenant.manager.service;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.NamespaceableResource;
import io.fabric8.openshift.client.OpenShiftClient;
import org.demo.tenant.manager.model.Subscription;
import org.demo.tenant.manager.model.User;
import org.demo.tenant.manager.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        String namespaceName = savedSubscription.getTenantName();
        String freshRssName = savedSubscription.getTenantName();
        String crdApiVersion = "freshrss.demo.openshift.com/v1alpha1";
        String crdKind = "FreshRSS";
        String defaultUser = user.getName();

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
            final CountDownLatch actionLatch = new CountDownLatch(1);

            Watch watch = ocpClient.genericKubernetesResources(crdApiVersion, crdKind)
                    .inNamespace(namespace.getMetadata().getName()).withName(freshRssName).watch(new Watcher<>() {
                        @Override
                        public void eventReceived(Action action, GenericKubernetesResource freshRSS) {
                            if (action == Action.ADDED) {
                                actionLatch.countDown();
                                freshRSS.getAdditionalProperties().entrySet().stream()
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
                        }

                        @Override
                        public void onClose(WatcherException e) {
                        }
                    });

            actionLatch.await(1, TimeUnit.MINUTES);
            watch.close();

            if (!urlList.isEmpty()) {
                System.out.println("URL --->" + urlList.get(0));
                savedSubscription.setUrl(urlList.get(0));
            }

            ocpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return subscriptionRepository.save(savedSubscription);
    }
}
