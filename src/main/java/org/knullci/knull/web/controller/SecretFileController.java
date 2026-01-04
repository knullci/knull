package org.knullci.knull.web.controller;

import org.knullci.knull.application.command.CreateSecretFileCommand;
import org.knullci.knull.application.command.DeleteSecretFileCommand;
import org.knullci.knull.application.dto.SecretFileDto;
import org.knullci.knull.application.interfaces.CreateSecretFileCommandHandler;
import org.knullci.knull.application.interfaces.DeleteSecretFileCommandHandler;
import org.knullci.knull.application.interfaces.GetAllSecretFilesQueryHandler;
import org.knullci.knull.application.interfaces.GetSecretFileQueryHandler;
import org.knullci.knull.application.query.GetAllSecretFilesQuery;
import org.knullci.knull.application.query.GetSecretFileQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/secret-files")
public class SecretFileController {

    private final CreateSecretFileCommandHandler createSecretFileCommandHandler;
    private final DeleteSecretFileCommandHandler deleteSecretFileCommandHandler;
    private final GetAllSecretFilesQueryHandler getAllSecretFilesQueryHandler;
    private final GetSecretFileQueryHandler getSecretFileQueryHandler;

    public SecretFileController(
            CreateSecretFileCommandHandler createSecretFileCommandHandler,
            DeleteSecretFileCommandHandler deleteSecretFileCommandHandler,
            GetAllSecretFilesQueryHandler getAllSecretFilesQueryHandler,
            GetSecretFileQueryHandler getSecretFileQueryHandler) {
        this.createSecretFileCommandHandler = createSecretFileCommandHandler;
        this.deleteSecretFileCommandHandler = deleteSecretFileCommandHandler;
        this.getAllSecretFilesQueryHandler = getAllSecretFilesQueryHandler;
        this.getSecretFileQueryHandler = getSecretFileQueryHandler;
    }

    @GetMapping
    public String listSecretFiles(Model model) {
        List<SecretFileDto> secretFiles = getAllSecretFilesQueryHandler.handle(new GetAllSecretFilesQuery());
        model.addAttribute("secretFiles", secretFiles);
        return "secret-files/index";
    }

    @GetMapping("/create")
    public String showCreateForm() {
        return "secret-files/create";
    }

    @PostMapping("/create")
    public String createSecretFile(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("type") String type,
            @RequestParam("content") String content) {

        CreateSecretFileCommand command = new CreateSecretFileCommand(
                name, description, type, content);

        createSecretFileCommandHandler.handle(command);

        return "redirect:/secret-files";
    }

    @GetMapping("/{id}")
    public String viewSecretFile(@PathVariable Long id, Model model) {
        SecretFileDto secretFile = getSecretFileQueryHandler.handle(new GetSecretFileQuery(id));
        model.addAttribute("secretFile", secretFile);
        return "secret-files/view";
    }

    @PostMapping("/{id}/delete")
    public String deleteSecretFile(@PathVariable Long id) {
        deleteSecretFileCommandHandler.handle(new DeleteSecretFileCommand(id));
        return "redirect:/secret-files";
    }
}
