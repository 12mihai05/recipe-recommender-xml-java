package org.seweb.reciperecommenderxmljava.controller;

import org.seweb.reciperecommenderxmljava.service.XmlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class HomeController {
    @Autowired
    private XmlService xmlService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalRecipes", xmlService.getAllRecipes().size());
        model.addAttribute("totalUsers", xmlService.getAllUsers().size());
        return "index";
    }
}

