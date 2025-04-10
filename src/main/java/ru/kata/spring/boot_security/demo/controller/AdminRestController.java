package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.exception.EmailAlreadyExistsException;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.model.UserDTO;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminRestController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/current-user")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            Map<String, String> errors = new HashMap<>();

            if (userDTO.getUsername() == null || userDTO.getUsername().length() < 3 || userDTO.getUsername().length() > 20) {
                errors.put("newUsername", "Username должен быть от 3 до 20 символов");
            }

            if (userDTO.getSurname() == null || userDTO.getSurname().length() < 2 || userDTO.getSurname().length() > 20) {
                errors.put("newSurname", "Surname должен быть от 2 до 20 символов");
            }

            if (userDTO.getAge() == null || userDTO.getAge() < 14) {
                errors.put("newAge", "Минимальный возраст - 14 лет");
            }

            if (userDTO.getEmail() == null || !userDTO.getEmail().matches("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
                errors.put("newEmail", "Некорректный формат email");
            }

            if (userDTO.getPassword() == null || userDTO.getPassword().length() < 4) {
                errors.put("newPassword", "Пароль должен содержать не менее 4 символов");
            }

            List<Long> roleIds = userDTO.getRoleIds();
            if (roleIds == null || roleIds.isEmpty()) {
                errors.put("newRole", "Выберите минимум одну роль");
            }

            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(errors);
            }

            User user = new User();
            user.setUsername(userDTO.getUsername());
            user.setSurname(userDTO.getSurname());
            user.setAge(userDTO.getAge());
            user.setEmail(userDTO.getEmail());
            user.setPassword(userDTO.getPassword());

            userService.saveUser(user, roleIds);
            return ResponseEntity.ok("User created successfully");
        } catch (EmailAlreadyExistsException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("newEmail", e.getMessage());
            return ResponseEntity.badRequest().body(errors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user: " + e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO userDTO,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            Map<String, String> errors = new HashMap<>();

            if (userDTO.getUsername() == null || userDTO.getUsername().length() < 3 || userDTO.getUsername().length() > 20) {
                errors.put("editUsername", "Username должен быть от 3 до 20 символов");
            }

            if (userDTO.getSurname() == null || userDTO.getSurname().length() < 2 || userDTO.getSurname().length() > 20) {
                errors.put("editSurname", "Surname должен быть от 2 до 20 символов");
            }

            if (userDTO.getAge() == null || userDTO.getAge() < 14) {
                errors.put("editAge", "Минимальный возраст - 14 лет");
            }

            if (userDTO.getEmail() == null || !userDTO.getEmail().matches("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
                errors.put("editEmail", "Некорректный формат email");
            }

            if (userDTO.getPassword() == null || userDTO.getPassword().length() < 4) {
                errors.put("editPassword", "Пароль должен содержать не менее 4 символов");
            }

            List<Long> roleIds = userDTO.getRoleIds();
            if (roleIds == null || roleIds.isEmpty()) {
                errors.put("editRole", "Выберите минимум одну роль");
            }

            if (currentUser.getId().equals(id) &&
                    (roleIds == null || roleIds.isEmpty() ||
                            roleIds.stream()
                                    .noneMatch(roleId -> roleService.getAllRolesByIds(List.of(roleId))
                                            .stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))))) {
                errors.put("editRole", "Администратор не может удалить свою роль ADMIN");
            }

            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(errors);
            }

            User user = new User();
            user.setId(id);
            user.setUsername(userDTO.getUsername());
            user.setSurname(userDTO.getSurname());
            user.setAge(userDTO.getAge());
            user.setEmail(userDTO.getEmail());
            user.setPassword(userDTO.getPassword());

            userService.updateUser(user, roleIds);
            return ResponseEntity.ok("User updated successfully");
        } catch (EmailAlreadyExistsException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("editEmail", e.getMessage());
            return ResponseEntity.badRequest().body(errors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user: " + e.getMessage());
        }
    }
}