package org.seweb.reciperecommenderxmljava.controller;

import org.seweb.reciperecommenderxmljava.service.XmlService;
import org.seweb.reciperecommenderxmljava.util.RecipeScraperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class HomeController {
    @Autowired
    private XmlService xmlService;

    @Autowired
    private RecipeScraperUtil recipeScraperUtil;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalRecipes", recipeScraperUtil.getScrapedRecipes().size());
        model.addAttribute("totalUsers", xmlService.getAllUsers().size());
        return "index";
    }
}

