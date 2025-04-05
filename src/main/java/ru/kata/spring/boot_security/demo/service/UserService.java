package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Set;

public interface UserService {
    boolean existsByUsername(String username);
    List<User> getAllUsers();
    User getUserById(Long id);
    boolean saveUser(User user, Set<Long> roleIds);
    void updateUser(Long id, User user, Set<Long> roleIds);
    void deleteUser(Long id);
}
