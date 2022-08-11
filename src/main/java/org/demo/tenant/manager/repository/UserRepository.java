package org.demo.tenant.manager.repository;

import org.demo.tenant.manager.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    @Override
    List<User> findAll();

    Optional<User> findUserByEmailAndPasswordAndStatus(String email, String pwd, boolean status);
}
