package org.demo.tenant.manager.controller;

import org.demo.tenant.manager.model.Tenant;
import org.demo.tenant.manager.service.SubscriptionService;
import org.demo.tenant.manager.service.TenantService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class TenantController {

    private final TenantService tenantService;

    private final SubscriptionService subscriptionService;

    public TenantController(TenantService tenantService, SubscriptionService subscriptionService) {
        this.tenantService = tenantService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }

    @GetMapping("/logout")
    public String getLogout(HttpSession session) {
        session.removeAttribute("tenant");
        return "login";
    }

    @GetMapping("/signup")
    public String getSignup() {
        return "signup";
    }

    @PostMapping("/login")
    public String submitLogin(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              HttpSession session) {
        System.out.println("going to check login db");
        Optional<Tenant> optionalUser = tenantService.findTenant(email, password, true);
        if (optionalUser.isPresent()) {
            session.setAttribute("tenant", optionalUser.get());
            return "redirect:/";
        }
        return "redirect:/login";
    }

    @PostMapping("/signup")
    public String submitSignup(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam("tenantName") String tenantName,
                               @RequestParam("orgName") String orgName,
                               @RequestParam("address") String address,
                               @RequestParam("phone") String phone,
                               @RequestParam("contactPerson") String contactPerson,
                               HttpSession session) {
        Tenant oldTenant = tenantService.save(email, password, tenantName, orgName, address, phone, contactPerson);
        subscriptionService.save(oldTenant);
        Optional<Tenant> latestTenant = tenantService.findLatestTenant(oldTenant);
        session.setAttribute("tenant", latestTenant.get());
        return "redirect:/";
    }
}
