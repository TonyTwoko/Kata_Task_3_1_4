package ru.kata.spring.boot_security.demo.service.impl;

import org.springframework.stereotype.Service;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.service.RoleService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Set<Role> getRolesByIds(Set<Long> roleIds) {
        return new HashSet<>(roleRepository.findAllById(roleIds));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
