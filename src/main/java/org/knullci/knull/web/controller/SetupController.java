package org.knullci.knull.web.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.knullci.knull.application.command.SetupAdminCommand;
import org.knullci.knull.application.dto.SetupResult;
import org.knullci.knull.application.interfaces.SetupAdminCommandHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for first-time setup of Knull CI/CD.
 * Delegates to SetupAdminCommandHandler for business logic.
 */
@Controller
@RequestMapping("/setup")
@RequiredArgsConstructor
public class SetupController {

    private final SetupAdminCommandHandler setupAdminCommandHandler;

    @GetMapping
    public String showSetupPage(Model model) {
        // If users already exist, redirect to login
        if (!setupAdminCommandHandler.isSetupRequired()) {
            return "redirect:/login";
        }

        if (!model.containsAttribute("setupForm")) {
            model.addAttribute("setupForm", new SetupForm());
        }
        if (!model.containsAttribute("errors")) {
            model.addAttribute("errors", new ArrayList<String>());
        }

        return "setup";
    }

    @PostMapping
    public String createAdminUser(
            @ModelAttribute("setupForm") SetupForm form,
            Model model,
            RedirectAttributes redirectAttributes) {

        // If users already exist, redirect to login
        if (!setupAdminCommandHandler.isSetupRequired()) {
            return "redirect:/login";
        }

        // Validate password confirmation at controller level (UI-specific validation)
        List<String> uiErrors = new ArrayList<>();
        if (form.getConfirmPassword() == null || form.getConfirmPassword().isEmpty()) {
            uiErrors.add("Password confirmation is required");
        } else if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            uiErrors.add("Passwords do not match");
        }

        if (!uiErrors.isEmpty()) {
            model.addAttribute("errors", uiErrors);
            model.addAttribute("setupForm", form);
            return "setup";
        }

        // Create command and delegate to handler
        SetupAdminCommand command = new SetupAdminCommand(
                form.getUsername(),
                form.getEmail(),
                form.getPassword(),
                form.getDisplayName());

        SetupResult result = setupAdminCommandHandler.handle(command);

        if (!result.isSuccess()) {
            model.addAttribute("errors", result.getErrors());
            model.addAttribute("setupForm", form);
            return "setup";
        }

        redirectAttributes.addFlashAttribute("setupSuccess", true);
        redirectAttributes.addFlashAttribute("adminUsername", result.getUsername());

        return "redirect:/login";
    }

    /**
     * Form object for admin user setup (view model).
     */
    @Getter
    @Setter
    public static class SetupForm {
        private String username;
        private String email;
        private String password;
        private String confirmPassword;
        private String displayName;
    }
}
