package org.demo.tenant.manager.service;

import org.demo.tenant.manager.model.User;
import org.demo.tenant.manager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User save(String name, String email, String pwd){
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(pwd);
        newUser.setStatus(true);

        return userRepository.save(newUser);
    }

    public Optional<User> findUser(String email, String pwd, boolean status) {
        return userRepository.findUserByEmailAndPasswordAndStatus(email, pwd, status);
    }

    public Optional<User> findLatestUser(User oldUser) {
        return userRepository.findById(oldUser.getId());
    }
}
