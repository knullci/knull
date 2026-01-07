package org.knullci.knull.web.controller;

import org.knullci.knull.application.command.CreateJobCommand;
import org.knullci.knull.application.command.DeleteJobCommand;
import org.knullci.knull.application.command.TriggerBuildCommand;
import org.knullci.knull.application.command.UpdateJobCommand;
import org.knullci.knull.application.interfaces.CreateJobCommandHandler;
import org.knullci.knull.application.interfaces.DeleteJobCommandHandler;
import org.knullci.knull.application.interfaces.GetAllCredentialsQueryHandler;
import org.knullci.knull.application.interfaces.GetAllQueryHandler;
import org.knullci.knull.application.interfaces.GetBuildsByJobIdQueryHandler;
import org.knullci.knull.application.interfaces.GetJobByIdQueryHandler;
import org.knullci.knull.application.interfaces.TriggerBuildCommandHandler;
import org.knullci.knull.application.interfaces.UpdateJobCommandHandler;
import org.knullci.knull.application.query.GetAllCredentialsQuery;
import org.knullci.knull.application.query.GetAllJobQuery;
import org.knullci.knull.application.query.GetBuildsByJobIdQuery;
import org.knullci.knull.application.query.GetJobByIdQuery;
import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.web.dto.JobForm;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/jobs")
public class JobController {

    private final CreateJobCommandHandler createJobCommandHandler;
    private final GetAllQueryHandler getAllQueryHandler;
    private final GetAllCredentialsQueryHandler getAllCredentialsQueryHandler;
    private final GetJobByIdQueryHandler getJobByIdQueryHandler;
    private final DeleteJobCommandHandler deleteJobCommandHandler;
    private final TriggerBuildCommandHandler triggerBuildCommandHandler;
    private final UpdateJobCommandHandler updateJobCommandHandler;
    private final GetBuildsByJobIdQueryHandler getBuildsByJobIdQueryHandler;

    public JobController(CreateJobCommandHandler createJobCommandHandler,
            GetAllQueryHandler getAllQueryHandler,
            GetAllCredentialsQueryHandler getAllCredentialsQueryHandler,
            GetJobByIdQueryHandler getJobByIdQueryHandler,
            DeleteJobCommandHandler deleteJobCommandHandler,
            TriggerBuildCommandHandler triggerBuildCommandHandler,
            UpdateJobCommandHandler updateJobCommandHandler,
            GetBuildsByJobIdQueryHandler getBuildsByJobIdQueryHandler) {
        this.createJobCommandHandler = createJobCommandHandler;
        this.getAllQueryHandler = getAllQueryHandler;
        this.getAllCredentialsQueryHandler = getAllCredentialsQueryHandler;
        this.getJobByIdQueryHandler = getJobByIdQueryHandler;
        this.deleteJobCommandHandler = deleteJobCommandHandler;
        this.triggerBuildCommandHandler = triggerBuildCommandHandler;
        this.updateJobCommandHandler = updateJobCommandHandler;
        this.getBuildsByJobIdQueryHandler = getBuildsByJobIdQueryHandler;
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
                jobForm.isCleanupWorkspace(),
                jobForm.isCheckoutLatestCommit(),
                jobForm.getGitRepository(),
                jobForm.getCredentialId(),
                jobForm.getBranch(),
                jobForm.getBranchPattern(),
                jobForm.getScriptFileLocation()));

        return "redirect:/jobs";
    }

    @GetMapping
    public String getAllJobs(Model model) {
        var jobs = getAllQueryHandler.handle(new GetAllJobQuery());
        model.addAttribute("jobs", jobs);

        return "jobs/index";
    }

    @GetMapping("/{id}")
    public String getJobById(@PathVariable("id") Long id, Model model) {
        try {
            var job = getJobByIdQueryHandler.handle(new GetJobByIdQuery(id));
            var builds = getBuildsByJobIdQueryHandler.handle(new GetBuildsByJobIdQuery(id));
            model.addAttribute("job", job);
            model.addAttribute("builds", builds);
            return "jobs/view";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Job not found with ID: " + id);
            return "redirect:/jobs";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditJob(@PathVariable("id") Long id, Model model) {
        try {
            var job = getJobByIdQueryHandler.handle(new GetJobByIdQuery(id));

            // Map JobDetailDto to JobForm for the form
            JobForm jobForm = new JobForm();
            jobForm.setName(job.getName());
            jobForm.setDescription(job.getDescription());
            jobForm.setJobType(job.getJobType());
            jobForm.setGitRepository(job.getGitRepository());
            jobForm.setCredentialId(job.getCredentialId());
            jobForm.setBranch(job.getBranch());
            jobForm.setBranchPattern(job.getBranchPattern());
            jobForm.setScriptFileLocation(job.getScriptFileLocation());
            jobForm.setCleanupWorkspace(job.isCleanupWorkspace());
            jobForm.setCheckoutLatestCommit(job.isCheckoutLatestCommit());

            model.addAttribute("jobForm", jobForm);
            model.addAttribute("jobId", id);
            model.addAttribute("jobTypes", JobType.values());

            var credentials = getAllCredentialsQueryHandler.handle(new GetAllCredentialsQuery());
            model.addAttribute("credentials", credentials);

            return "jobs/edit";
        } catch (RuntimeException e) {
            return "redirect:/jobs";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateJob(@PathVariable("id") Long id, @ModelAttribute("jobForm") JobForm jobForm,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("jobId", id);
            model.addAttribute("jobTypes", JobType.values());
            var credentials = getAllCredentialsQueryHandler.handle(new GetAllCredentialsQuery());
            model.addAttribute("credentials", credentials);
            return "jobs/edit";
        }

        try {
            updateJobCommandHandler.handle(new UpdateJobCommand(
                    id,
                    jobForm.getName(),
                    jobForm.getDescription(),
                    jobForm.getJobType(),
                    jobForm.isCleanupWorkspace(),
                    jobForm.isCheckoutLatestCommit(),
                    jobForm.getGitRepository(),
                    jobForm.getCredentialId(),
                    jobForm.getBranch(),
                    jobForm.getBranchPattern(),
                    jobForm.getScriptFileLocation()));

            redirectAttributes.addFlashAttribute("successMessage", "Job updated successfully!");
            return "redirect:/jobs/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update job: " + e.getMessage());
            return "redirect:/jobs/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteJob(@PathVariable("id") Long id) {
        deleteJobCommandHandler.handle(new DeleteJobCommand(id));
        return "redirect:/jobs";
    }

    @PostMapping("/{id}/trigger")
    public String triggerBuild(@PathVariable("id") Long id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String triggeredBy = authentication != null ? authentication.getName() : "Manual";
            triggerBuildCommandHandler.handle(new TriggerBuildCommand(id, triggeredBy));
            redirectAttributes.addFlashAttribute("successMessage", "Build triggered successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to trigger build: " + e.getMessage());
        }
        return "redirect:/builds";
    }

}
