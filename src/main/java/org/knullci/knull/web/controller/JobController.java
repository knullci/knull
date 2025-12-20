package org.knullci.knull.web.controller;

import org.knullci.knull.application.command.CreateJobCommand;
import org.knullci.knull.application.command.DeleteJobCommand;
import org.knullci.knull.application.interfaces.CreateJobCommandHandler;
import org.knullci.knull.application.interfaces.DeleteJobCommandHandler;
import org.knullci.knull.application.interfaces.GetAllCredentialsQueryHandler;
import org.knullci.knull.application.interfaces.GetAllQueryHandler;
import org.knullci.knull.application.interfaces.GetJobByIdQueryHandler;
import org.knullci.knull.application.query.GetAllCredentialsQuery;
import org.knullci.knull.application.query.GetAllJobQuery;
import org.knullci.knull.application.query.GetJobByIdQuery;
import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.web.dto.JobForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/jobs")
public class JobController {

    private final CreateJobCommandHandler createJobCommandHandler;
    private final GetAllQueryHandler getAllQueryHandler;
    private final GetAllCredentialsQueryHandler getAllCredentialsQueryHandler;
    private final GetJobByIdQueryHandler getJobByIdQueryHandler;
    private final DeleteJobCommandHandler deleteJobCommandHandler;

    public JobController(CreateJobCommandHandler createJobCommandHandler, 
                        GetAllQueryHandler getAllQueryHandler,
                        GetAllCredentialsQueryHandler getAllCredentialsQueryHandler,
                        GetJobByIdQueryHandler getJobByIdQueryHandler,
                        DeleteJobCommandHandler deleteJobCommandHandler) {
        this.createJobCommandHandler = createJobCommandHandler;
        this.getAllQueryHandler = getAllQueryHandler;
        this.getAllCredentialsQueryHandler = getAllCredentialsQueryHandler;
        this.getJobByIdQueryHandler = getJobByIdQueryHandler;
        this.deleteJobCommandHandler = deleteJobCommandHandler;
    }

    @GetMapping("/create")
    public String showCreateJob(Model model) {
        model.addAttribute("jobForm", new JobForm());
        model.addAttribute("jobTypes", JobType.values());
        
        // Fetch all credentials for the dropdown
        var credentials = getAllCredentialsQueryHandler.handle(new GetAllCredentialsQuery());
        model.addAttribute("credentials", credentials);

        return "jobs/create";
    }

    @PostMapping
    public String createJob(@ModelAttribute("jobForm") JobForm jobForm, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("jobTypes", JobType.values());
            var credentials = getAllCredentialsQueryHandler.handle(new GetAllCredentialsQuery());
            model.addAttribute("credentials", credentials);
            return "jobs/create";
        }

        createJobCommandHandler.handle(new CreateJobCommand(
                jobForm.getName(),
                jobForm.getDescription(),
                jobForm.getJobType(),
                jobForm.getGitRepository(),
                jobForm.getCredentialId(),
                jobForm.getBranch(),
                jobForm.getBranchPattern(),
                jobForm.getScriptFileLocation()
        ));

        return "redirect:/jobs";
    }

    @GetMapping
    public String getAllJobs(Model model) {
        var jobs = getAllQueryHandler.handle(new GetAllJobQuery());
        model.addAttribute("jobs", jobs);

        return "jobs/index";
    }

    @GetMapping("/{id}")
    public String getJobById(@PathVariable Long id, Model model) {
        try {
            var job = getJobByIdQueryHandler.handle(new GetJobByIdQuery(id));
            model.addAttribute("job", job);
            return "jobs/view";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Job not found with ID: " + id);
            return "redirect:/jobs";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteJob(@PathVariable Long id) {
        deleteJobCommandHandler.handle(new DeleteJobCommand(id));
        return "redirect:/jobs";
    }

}
