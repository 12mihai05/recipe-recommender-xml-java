package org.seweb.reciperecommenderxmljava.controller;

import org.seweb.reciperecommenderxmljava.model.Recipe;
import org.seweb.reciperecommenderxmljava.model.AppUser;
import org.seweb.reciperecommenderxmljava.service.XmlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class RecipeController {

    @Autowired
    private XmlService xmlService;

    @GetMapping("/recipes")
    public String allRecipes(Model model) {
        List<Recipe> recipes = xmlService.getAllRecipes();
        model.addAttribute("recipes", recipes);
        return "recipes";
    }

    @GetMapping("/users")
    public String allUsers(Model model) {
        List<AppUser> users = xmlService.getAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/recipe/{id}")
    public String recipeDetails(@PathVariable int id, Model model) {
        Recipe recipe = xmlService.getRecipeById(id);
        model.addAttribute("recipe", recipe);
        return "recipe-detail";
    }

    @GetMapping("/recipes/by-cuisine")
    public String recipesByCuisine(@RequestParam(required = false) String cuisine, Model model) {
        List<Recipe> recipes = new ArrayList<>();

        if (cuisine != null && !cuisine.isBlank()) {
            recipes = xmlService.getRecipesByCuisine(cuisine);
        }

        model.addAttribute("recipes", recipes);
        model.addAttribute("cuisine", cuisine);
        return "recipes-by-cuisine";
    }

    @GetMapping("/recommendations")
    public String recommendations(Model model) {
        AppUser firstUser = xmlService.getFirstUser();
        List<Recipe> recommendedRecipes = null;

        if (firstUser != null) {
            recommendedRecipes = xmlService.getRecipesBySkillAndCuisine(
                firstUser.getSkillLevel(),
                firstUser.getPreferredCuisineType()
            );
        }

        model.addAttribute("user", firstUser);
        model.addAttribute("recipes", recommendedRecipes);
        return "recommendations";
    }
}


