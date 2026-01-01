package org.knullci.knull.web.controller;

import org.knullci.knull.application.command.CancelBuildCommand;
import org.knullci.knull.application.dto.CancelBuildResult;
import org.knullci.knull.application.interfaces.CancelBuildCommandHandler;
import org.knullci.knull.application.interfaces.GetBuildsByJobIdQueryHandler;
import org.knullci.knull.application.query.GetBuildsByJobIdQuery;
import org.knullci.knull.domain.repository.BuildRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Controller
@RequestMapping("/builds")
public class BuildController {

    private final BuildRepository buildRepository;
    private final GetBuildsByJobIdQueryHandler getBuildsByJobIdQueryHandler;
    private final CancelBuildCommandHandler cancelBuildCommandHandler;

    public BuildController(BuildRepository buildRepository,
            GetBuildsByJobIdQueryHandler getBuildsByJobIdQueryHandler,
            CancelBuildCommandHandler cancelBuildCommandHandler) {
        this.buildRepository = buildRepository;
        this.getBuildsByJobIdQueryHandler = getBuildsByJobIdQueryHandler;
        this.cancelBuildCommandHandler = cancelBuildCommandHandler;
    }

    @GetMapping
    public String getAllBuilds(@RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model) {
        var builds = buildRepository.findAllPaginated(page, size);
        var totalBuilds = buildRepository.countAll();
        var totalPages = (int) Math.ceil((double) totalBuilds / size);

        model.addAttribute("builds", builds);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalBuilds", totalBuilds);

        return "builds/index";
    }

    @GetMapping("/{id}")
    public String redirectToOverview(@PathVariable("id") Long id) {
        return "redirect:/builds/" + id + "/overview";
    }

    @GetMapping("/{id}/overview")
    public String getBuildOverview(@PathVariable("id") Long id, Model model) {
        return renderBuildSection(id, "overview", model);
    }

    @GetMapping("/{id}/steps")
    public String getBuildSteps(@PathVariable("id") Long id, Model model) {
        return renderBuildSection(id, "steps", model);
    }

    @GetMapping("/{id}/logs")
    public String getBuildLogs(@PathVariable("id") Long id, Model model) {
        return renderBuildSection(id, "logs", model);
    }

    @GetMapping("/{id}/pipeline")
    public String getBuildPipeline(@PathVariable("id") Long id, Model model) {
        var build = buildRepository.findById(id);
        if (build.isEmpty()) {
            return "redirect:/builds";
        }
        model.addAttribute("build", build.get());
        return "builds/pipeline";
    }

    @GetMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getBuildStatus(@PathVariable("id") Long id) {
        var build = buildRepository.findById(id);
        return build.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/{id}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> cancelBuild(@PathVariable("id") Long id) {
        CancelBuildResult result = cancelBuildCommandHandler.handle(new CancelBuildCommand(id));

        if (result.isSuccess()) {
            return ResponseEntity.ok(Map.of("success", true, "message", result.getMessage()));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", result.getMessage()));
        }
    }

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamBuildEvents(@PathVariable("id") Long id) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout

        new Thread(() -> {
            try {
                while (true) {
                    var buildOpt = buildRepository.findById(id);
                    if (buildOpt.isEmpty()) {
                        emitter.complete();
                        return;
                    }

                    // Send build data as JSON
                    emitter.send(buildOpt.get(), MediaType.APPLICATION_JSON);

                    var status = buildOpt.get().getStatus();
                    if (status != null && status.name().matches("SUCCESS|FAILURE|CANCELLED")) {
                        emitter.complete();
                        return;
                    }

                    Thread.sleep(1000);
                }
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }).start();

        return emitter;
    }

    @GetMapping("/job/{jobId}")
    public String getBuildsByJobId(@PathVariable("jobId") Long jobId, Model model) {
        var builds = getBuildsByJobIdQueryHandler.handle(new GetBuildsByJobIdQuery(jobId));
        model.addAttribute("builds", builds);
        model.addAttribute("jobId", jobId);
        return "builds/index";
    }

    private String renderBuildSection(Long id, String section, Model model) {
        var build = buildRepository.findById(id);
        if (build.isEmpty()) {
            model.addAttribute("errorMessage", "Build not found with ID: " + id);
            return "redirect:/builds";
        }

        model.addAttribute("build", build.get());
        model.addAttribute("activeSection", section);
        return "builds/view";
    }
}
