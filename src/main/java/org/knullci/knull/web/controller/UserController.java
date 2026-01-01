package org.knullci.knull.web.controller;

import org.knullci.knull.application.command.CreateUserCommand;
import org.knullci.knull.application.command.DeleteUserCommand;
import org.knullci.knull.application.command.ToggleUserLockCommand;
import org.knullci.knull.application.command.UpdateUserCommand;
import org.knullci.knull.application.dto.UserDto;
import org.knullci.knull.application.handler.CreateUserCommandHandler;
import org.knullci.knull.application.handler.UpdateUserCommandHandler;
import org.knullci.knull.application.interfaces.DeleteUserCommandHandler;
import org.knullci.knull.application.interfaces.ToggleUserLockCommandHandler;
import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;
import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;
import org.knullci.knull.web.form.UserForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for user management (Admin only).
 */
@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final CreateUserCommandHandler createUserCommandHandler;
    private final UpdateUserCommandHandler updateUserCommandHandler;
    private final DeleteUserCommandHandler deleteUserCommandHandler;
    private final ToggleUserLockCommandHandler toggleUserLockCommandHandler;

    public UserController(UserRepository userRepository,
            CreateUserCommandHandler createUserCommandHandler,
            UpdateUserCommandHandler updateUserCommandHandler,
            DeleteUserCommandHandler deleteUserCommandHandler,
            ToggleUserLockCommandHandler toggleUserLockCommandHandler) {
        this.userRepository = userRepository;
        this.createUserCommandHandler = createUserCommandHandler;
        this.updateUserCommandHandler = updateUserCommandHandler;
        this.deleteUserCommandHandler = deleteUserCommandHandler;
        this.toggleUserLockCommandHandler = toggleUserLockCommandHandler;
    }

    @GetMapping
    public String listUsers(Model model) {
        logger.info("Listing all users");

        List<UserDto> users = userRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        return "users/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        model.addAttribute("roles", Role.values());
        model.addAttribute("permissions", Permission.values());
        return "users/create";
    }

    @PostMapping
    public String createUser(@ModelAttribute UserForm form, RedirectAttributes redirectAttributes) {
        logger.info("Creating new user: {}", form.getUsername());

        try {
            CreateUserCommand command = new CreateUserCommand(
                    form.getUsername(),
                    form.getEmail(),
                    form.getPassword(),
                    form.getDisplayName(),
                    form.getRole(),
                    form.getAdditionalPermissions() != null ? new HashSet<>(form.getAdditionalPermissions()) : null);

            createUserCommandHandler.handle(command);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");

        } catch (Exception e) {
            logger.error("Failed to create user", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create user: " + e.getMessage());
        }

        return "redirect:/users";
    }

    @GetMapping("/{id}")
    public String showUser(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        model.addAttribute("user", toDto(user));
        return "users/view";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        UserForm form = new UserForm();
        form.setId(user.getId());
        form.setUsername(user.getUsername());
        form.setEmail(user.getEmail());
        form.setDisplayName(user.getDisplayName());
        form.setRole(user.getRole());
        form.setAdditionalPermissions(
                user.getAdditionalPermissions() != null
                        ? user.getAdditionalPermissions().stream().collect(Collectors.toList())
                        : null);
        form.setActive(user.isActive());
        form.setAccountLocked(user.isAccountLocked());

        model.addAttribute("userForm", form);
        model.addAttribute("roles", Role.values());
        model.addAttribute("permissions", Permission.values());
        return "users/edit";
    }

    @PostMapping("/{id}")
    public String updateUser(@PathVariable Long id,
            @ModelAttribute UserForm form,
            RedirectAttributes redirectAttributes) {
        logger.info("Updating user: {}", id);

        try {
            UpdateUserCommand command = new UpdateUserCommand(
                    id,
                    form.getEmail(),
                    form.getDisplayName(),
                    form.getRole(),
                    form.getAdditionalPermissions() != null ? new HashSet<>(form.getAdditionalPermissions()) : null,
                    form.isActive(),
                    form.isAccountLocked());

            updateUserCommandHandler.handle(command);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");

        } catch (Exception e) {
            logger.error("Failed to update user", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update user: " + e.getMessage());
        }

        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Deleting user: {}", id);

        try {
            deleteUserCommandHandler.handle(new DeleteUserCommand(id));
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            logger.error("Failed to delete user", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete user: " + e.getMessage());
        }

        return "redirect:/users";
    }

    @PostMapping("/{id}/toggle-lock")
    public String toggleLock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Toggling lock status for user: {}", id);

        try {
            boolean isNowLocked = toggleUserLockCommandHandler.handle(new ToggleUserLockCommand(id));
            redirectAttributes.addFlashAttribute("successMessage",
                    isNowLocked ? "User locked!" : "User unlocked!");

        } catch (Exception e) {
            logger.error("Failed to toggle user lock status", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed: " + e.getMessage());
        }

        return "redirect:/users";
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getAdditionalPermissions(),
                user.isActive(),
                user.isAccountLocked(),
                user.getCreatedAt(),
                user.getLastLoginAt());
    }
}
