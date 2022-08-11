package org.demo.tenant.manager.controller;

import org.demo.tenant.manager.service.UserService;
import org.demo.tenant.manager.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }

    @GetMapping("/logout")
    public String getLogout(HttpSession session) {
        session.removeAttribute("user");
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
        Optional<User> optionalUser = userService.findUser(email, password, true);
        if (optionalUser.isPresent()) {
            session.setAttribute("user", optionalUser.get());
            return "redirect:/";
        }
        return "redirect:/login";
    }

    @PostMapping("/signup")
    public String submitSignup(@RequestParam("name") String name,
                               @RequestParam("email") String email,
                               @RequestParam("password") String password,
                               HttpSession session) {
        System.out.println("saved a nw user in db");
        User user = userService.save(name, email, password);
        session.setAttribute("user", user);
        return "redirect:/";
    }
}
