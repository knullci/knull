package org.knullci.knull.web.controller;

import org.knullci.knull.application.command.SaveSettingsCommand;
import org.knullci.knull.application.interfaces.GetAllCredentialsQueryHandler;
import org.knullci.knull.application.interfaces.GetSettingsQueryHandler;
import org.knullci.knull.application.interfaces.SaveSettingsCommandHandler;
import org.knullci.knull.application.query.GetAllCredentialsQuery;
import org.knullci.knull.application.query.GetSettingsQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final GetSettingsQueryHandler getSettingsQueryHandler;
    private final SaveSettingsCommandHandler saveSettingsCommandHandler;
    private final GetAllCredentialsQueryHandler getAllCredentialsQueryHandler;

    public SettingsController(GetSettingsQueryHandler getSettingsQueryHandler,
            SaveSettingsCommandHandler saveSettingsCommandHandler,
            GetAllCredentialsQueryHandler getAllCredentialsQueryHandler) {
        this.getSettingsQueryHandler = getSettingsQueryHandler;
        this.saveSettingsCommandHandler = saveSettingsCommandHandler;
        this.getAllCredentialsQueryHandler = getAllCredentialsQueryHandler;
    }

    @GetMapping
    public String showSettings(Model model) {
        var settings = getSettingsQueryHandler.handle(new GetSettingsQuery());

        model.addAttribute("settings", settings);

        return "settings/index";
    }

    @GetMapping("/github")
    public String showGitHubSettings(Model model) {
        var settings = getSettingsQueryHandler.handle(new GetSettingsQuery());
        var credentials = getAllCredentialsQueryHandler.handle(new GetAllCredentialsQuery());

        // Find the selected credential if one is configured
        var selectedCredential = credentials.stream()
                .filter(cred -> settings.getGithubCredentialId() != null &&
                        cred.getId().equals(settings.getGithubCredentialId()))
                .findFirst()
                .orElse(null);

        model.addAttribute("settings", settings);
        model.addAttribute("credentials", credentials);
        model.addAttribute("selectedCredential", selectedCredential);

        return "settings/github";
    }

    @PostMapping("/github")
    public String saveGitHubSettings(
            @RequestParam(value = "githubCredentialId", required = false) Long githubCredentialId) {
        saveSettingsCommandHandler.handle(new SaveSettingsCommand(githubCredentialId));
        return "redirect:/settings/github";
    }

    @GetMapping("/general")
    public String showGeneralSettings(Model model) {
        var settings = getSettingsQueryHandler.handle(new GetSettingsQuery());

        // Get available timezones
        List<String> timezones = ZoneId.getAvailableZoneIds().stream()
                .sorted()
                .collect(Collectors.toList());

        model.addAttribute("settings", settings);
        model.addAttribute("timezones", timezones);

        return "settings/general";
    }

    @PostMapping("/general")
    public String saveGeneralSettings(
            @RequestParam(value = "instanceName", required = false) String instanceName,
            @RequestParam(value = "timezone", required = false) String timezone,
            @RequestParam(value = "maxConcurrentBuilds", required = false) Integer maxConcurrentBuilds,
            @RequestParam(value = "buildTimeoutMinutes", required = false) Integer buildTimeoutMinutes,
            @RequestParam(value = "buildRetentionDays", required = false) Integer buildRetentionDays,
            @RequestParam(value = "autoCleanupWorkspace", required = false) Boolean autoCleanupWorkspace,
            RedirectAttributes redirectAttributes) {

        // Handle checkbox - if not present in form, it's unchecked (false)
        if (autoCleanupWorkspace == null) {
            autoCleanupWorkspace = false;
        }

        SaveSettingsCommand command = new SaveSettingsCommand(
                null, // Don't change GitHub credential
                instanceName,
                timezone,
                maxConcurrentBuilds,
                buildTimeoutMinutes,
                buildRetentionDays,
                autoCleanupWorkspace);

        saveSettingsCommandHandler.handle(command);

        redirectAttributes.addFlashAttribute("success", "Settings saved successfully");
        return "redirect:/settings/general";
    }
}
