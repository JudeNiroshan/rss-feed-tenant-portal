package org.demo.tenant.manager.controller;

import org.demo.tenant.manager.service.UserService;
import org.demo.tenant.manager.model.User;
import org.demo.tenant.manager.service.SubscriptionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class TenantController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;

    public TenantController(UserService userService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/")
    public String get(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "index";
        }
        return "redirect:/login";
    }

    @GetMapping("/tenant-registration")
    public String getTenantRegistrationForm(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "register";
        }
        return "redirect:/login";
    }


    @PostMapping("/tenant-registration")
    public String submitSignup(@RequestParam("tenantName") String tenantName,
                               @RequestParam("currentSubscriptionUrl") String currentSubscriptionUrl,
                               @RequestParam("serviceLevel") String serviceLevel,
                               @RequestParam("url") String url,
                               HttpSession session) {
        if(!(session.getAttribute("user") instanceof User)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        subscriptionService.save(tenantName, currentSubscriptionUrl, serviceLevel, user);

        Optional<User> optionalUser = userService.findLatestUser(user);
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        session.setAttribute("user", optionalUser.get());
        return "redirect:/";
    }
}
