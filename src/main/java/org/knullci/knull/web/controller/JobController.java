package org.knullci.knull.web.controller;

import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.web.dto.JobForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/jobs")
public class JobController {

    @GetMapping("/create")
    public String showCreateJob(Model model) {
        model.addAttribute("jobForm", new JobForm());
        model.addAttribute("jobTypes", JobType.values());

        return "jobs/create";
    }

}
