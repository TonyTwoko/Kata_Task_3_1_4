package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.EmailAlreadyExistsException;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.validation.Valid;
import java.util.*;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String getAdminPage(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("allRoles", roleService.getAllRoles());
        model.addAttribute("newUser", new User());
        model.addAttribute("editUser", new User());
        model.addAttribute("newUserTabActive", false);
        model.addAttribute("editUserTabActive", false);
        return "admin";
    }

    @PostMapping
    public String createUser(
            @ModelAttribute("newUser") @Valid User user,
            BindingResult bindingResult,
            @RequestParam(value = "roleIds", required = false) Long[] roleIds,
            Model model,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        boolean rolesEmpty = roleIds == null || roleIds.length == 0;

        if (bindingResult.hasErrors() || rolesEmpty) {
            model.addAttribute("newUserTabActive", true);
            model.addAttribute("editUserTabActive", false);
            model.addAttribute("user", currentUser);
            model.addAttribute("users", userService.findAllUsers());
            model.addAttribute("allRoles", roleService.getAllRoles());
            model.addAttribute("newUser", user);
            model.addAttribute("editUser", new User());

            model.addAttribute("usernameError", getFieldError(bindingResult, "username"));
            model.addAttribute("surnameError", getFieldError(bindingResult, "surname"));
            model.addAttribute("ageError", getFieldError(bindingResult, "age"));
            model.addAttribute("emailError", getFieldError(bindingResult, "email"));
            model.addAttribute("passwordError", getFieldError(bindingResult, "password"));
            model.addAttribute("roleError", rolesEmpty ? "Выберите минимум одну роль" : null);

            return "admin";
        }

        try {
            userService.saveUser(user, Arrays.asList(roleIds));
        } catch (EmailAlreadyExistsException e) {
            model.addAttribute("newUserTabActive", true);
            model.addAttribute("editUserTabActive", false);
            model.addAttribute("user", currentUser);
            model.addAttribute("users", userService.findAllUsers());
            model.addAttribute("allRoles", roleService.getAllRoles());
            model.addAttribute("emailError", e.getMessage());
            model.addAttribute("newUser", user);
            model.addAttribute("editUser", new User());
            return "admin";
        }

        return "redirect:/admin";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute("editUser") @Valid User user,
            BindingResult bindingResult,
            @RequestParam(value = "roleIds", required = false) Long[] roleIds,
            @AuthenticationPrincipal User currentUser,
            Model model
    ) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        boolean rolesEmpty = roleIds == null || roleIds.length == 0;

        if (currentUser.getId().equals(id) &&
                (rolesEmpty || Arrays.stream(roleIds)
                        .noneMatch(roleId -> roleService.getAllRolesByIds(List.of(roleId))
                                .stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))))) {
            model.addAttribute("editRoleError", "Администратор не может удалить свою роль ADMIN");
            model.addAttribute("user", currentUser);
            model.addAttribute("users", userService.findAllUsers());
            model.addAttribute("allRoles", roleService.getAllRoles());
            model.addAttribute("editUserTabActive", true);
            model.addAttribute("newUserTabActive", false);
            model.addAttribute("editUser", user);
            model.addAttribute("newUser", new User());
            return "admin";
        }

        if (bindingResult.hasErrors() || rolesEmpty) {
            model.addAttribute("user", currentUser);
            model.addAttribute("users", userService.findAllUsers());
            model.addAttribute("allRoles", roleService.getAllRoles());
            model.addAttribute("editUserTabActive", true);
            model.addAttribute("newUserTabActive", false);
            model.addAttribute("editUser", user);
            model.addAttribute("newUser", new User());

            model.addAttribute("editUsernameError", getFieldError(bindingResult, "username"));
            model.addAttribute("editSurnameError", getFieldError(bindingResult, "surname"));
            model.addAttribute("editAgeError", getFieldError(bindingResult, "age"));
            model.addAttribute("editEmailError", getFieldError(bindingResult, "email"));
            model.addAttribute("editPasswordError", getFieldError(bindingResult, "password"));
            model.addAttribute("editRoleError", rolesEmpty ? "Выберите минимум одну роль" : null);

            return "admin";
        }

        user.setId(id);

        try {
            userService.updateUser(user, Arrays.asList(roleIds));
        } catch (EmailAlreadyExistsException e) {
            model.addAttribute("editEmailError", e.getMessage());
            model.addAttribute("user", currentUser);
            model.addAttribute("users", userService.findAllUsers());
            model.addAttribute("allRoles", roleService.getAllRoles());
            model.addAttribute("editUserTabActive", true);
            model.addAttribute("newUserTabActive", false);
            model.addAttribute("editUser", user);
            model.addAttribute("newUser", new User());
            return "admin";
        }

        return "redirect:/admin";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam Long id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return "redirect:/login";
        }
        userService.deleteUser(id);
        return "redirect:/admin";
    }


    private String getFieldError(BindingResult result, String field) {
        if (result.hasFieldErrors(field)) {
            return result.getFieldError(field).getDefaultMessage();
        }
        return null;
    }
}
