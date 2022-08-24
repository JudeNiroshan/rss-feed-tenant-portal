package org.demo.tenant.manager.controller;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Controller
public class TestController {

    @GetMapping("/test")
    public String getTenantRegistrationForm(HttpSession session) {

        String namespaceName = "jude-test-tenant-1";
        String freshRssName = "jude-freshrss-sample";
        String crdApiVersion = "freshrss.demo.openshift.com/v1alpha1";
        String crdKind = "FreshRSS";

        try {
            final KubernetesClient k8sClient = new KubernetesClientBuilder().build();
            // Do stuff with client
            OpenShiftClient ocpClient = k8sClient.adapt(OpenShiftClient.class);
            Namespace namespace = new NamespaceBuilder()
                    .withNewMetadata()
                    .withName(namespaceName)
                    .endMetadata()
                    .build();
            ocpClient.namespaces().createOrReplace(namespace);

            Map<String, Object> map = new HashMap<>();
            map.put("defaultUser", "bob");
            map.put("title", "Awesome RSS 123");

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

            actionLatch.await();
            watch.close();

            System.out.println("URL --->" + urlList.get(0));

            ocpClient.close();
            k8sClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return "login";
    }

}
