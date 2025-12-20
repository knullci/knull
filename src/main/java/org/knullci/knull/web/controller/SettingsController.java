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
        var credentials = getAllCredentialsQueryHandler.handle(new GetAllCredentialsQuery());
        
        model.addAttribute("settings", settings);
        model.addAttribute("credentials", credentials);

        return "settings/index";
    }

    @PostMapping
    public String saveSettings(@RequestParam("githubCredentialId") Long githubCredentialId) {
        saveSettingsCommandHandler.handle(new SaveSettingsCommand(githubCredentialId));
        return "redirect:/settings";
    }

}
