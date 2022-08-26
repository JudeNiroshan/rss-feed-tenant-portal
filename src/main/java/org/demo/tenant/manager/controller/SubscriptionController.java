package org.demo.tenant.manager.controller;

import org.demo.tenant.manager.model.Tenant;
import org.demo.tenant.manager.service.TenantService;
import org.demo.tenant.manager.service.SubscriptionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class SubscriptionController {

    private final TenantService tenantService;
    private final SubscriptionService subscriptionService;

    public SubscriptionController(TenantService tenantService, SubscriptionService subscriptionService) {
        this.tenantService = tenantService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/")
    public String get(HttpSession session) {
        if (session.getAttribute("tenant") != null) {
            return "index";
        }
        return "redirect:/login";
    }

    @GetMapping("/tenant-registration")
    public String getTenantRegistrationForm(HttpSession session) {
        if (session.getAttribute("tenant") != null) {
            return "register";
        }
        return "redirect:/login";
    }


    @PostMapping("/tenant-registration")
    public String submitSignup(@RequestParam("serviceLevel") String serviceLevel,
                               HttpSession session) {
        if(!(session.getAttribute("tenant") instanceof Tenant)) {
            return "redirect:/login";
        }
        Tenant tenant = (Tenant) session.getAttribute("tenant");
        subscriptionService.save(tenant);

        Optional<Tenant> optionalUser = tenantService.findLatestTenant(tenant);
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        session.setAttribute("tenant", optionalUser.get());
        return "redirect:/";
    }
}
