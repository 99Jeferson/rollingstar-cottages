package com.rollingstar.cottages.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auditing")
public class AuditingController {

    @GetMapping
    public String showBossPortal() {
        return "boss-audit"; // This will look for templates/boss-audit.html
    }
}