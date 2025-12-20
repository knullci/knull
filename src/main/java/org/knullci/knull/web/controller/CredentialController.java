package org.knullci.knull.web.controller;

import org.knullci.knull.application.command.CreateCredentialCommand;
import org.knullci.knull.application.interfaces.CreateCredentialCommandHandler;
import org.knullci.knull.application.interfaces.GetAllCredentialsQueryHandler;
import org.knullci.knull.application.query.GetAllCredentialsQuery;
import org.knullci.knull.domain.enums.CredentialType;
import org.knullci.knull.web.dto.CredentialForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/credentials")
public class CredentialController {

    private final CreateCredentialCommandHandler createCredentialCommandHandler;
    private final GetAllCredentialsQueryHandler getAllCredentialsQueryHandler;

    public CredentialController(CreateCredentialCommandHandler createCredentialCommandHandler,
                                GetAllCredentialsQueryHandler getAllCredentialsQueryHandler) {
        this.createCredentialCommandHandler = createCredentialCommandHandler;
        this.getAllCredentialsQueryHandler = getAllCredentialsQueryHandler;
    }

    @GetMapping("/create")
    public String showCreateCredential(Model model) {
        model.addAttribute("credentialForm", new CredentialForm());
        model.addAttribute("credentialTypes", CredentialType.values());
        return "credentials/create";
    }

    @PostMapping
    public String createCredential(@ModelAttribute("credentialForm") CredentialForm credentialForm,
                                    BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("credentialTypes", CredentialType.values());
            return "credentials/create";
        }

        createCredentialCommandHandler.handle(new CreateCredentialCommand(
                credentialForm.getName(),
                credentialForm.getDescription(),
                credentialForm.getCredentialType(),
                credentialForm.getUsername(),
                credentialForm.getPassword(),
                credentialForm.getToken()
        ));

        return "redirect:/credentials";
    }

    @GetMapping
    public String getAllCredentials(Model model) {
        var credentials = getAllCredentialsQueryHandler.handle(new GetAllCredentialsQuery());
        model.addAttribute("credentials", credentials);
        return "credentials/index";
    }
}
