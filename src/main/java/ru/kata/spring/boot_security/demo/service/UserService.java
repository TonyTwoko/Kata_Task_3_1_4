package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAllUsers();
    User findById(Long id);
    void saveUser(User user, List<Long> roleIds);
    void updateUser(User user, List<Long> roleIds);
    void deleteUser(Long id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
