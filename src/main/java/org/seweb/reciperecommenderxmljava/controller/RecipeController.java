package org.seweb.reciperecommenderxmljava.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.seweb.reciperecommenderxmljava.model.Recipe;
import org.seweb.reciperecommenderxmljava.model.AppUser;
import org.seweb.reciperecommenderxmljava.service.XmlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
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

    @PostMapping("/recipes/delete/{id}")
    public String deleteRecipe(@PathVariable int id) {
        xmlService.deleteRecipe(id);
        return "redirect:/recipes";
    }

    @GetMapping("/recipes/add")
    public String showAddRecipe() {
        return "add-recipe";

    }

    @PostMapping("/recipes/add")
    public String addRecipe(@RequestParam String title,
                            @RequestParam String cuisineType1,
                            @RequestParam String cuisineType2,
                            @RequestParam String difficulty,
                            Model model) {

        if (title == null || title.trim().isEmpty()
                || cuisineType1 == null || cuisineType1.trim().isEmpty()
                || cuisineType2 == null || cuisineType2.trim().isEmpty()
                || difficulty == null || difficulty.trim().isEmpty()) {

            model.addAttribute("error", "All fields are required!");
            return "add-recipe";
        }
        Recipe recipe = new Recipe(
                xmlService.generateNextRecipeId(),
                title,
                cuisineType1,
                cuisineType2,
                difficulty
        );

        xmlService.saveRecipe(recipe);
        return "redirect:/recipes";
    }

    @GetMapping("/xsl/recipes/view")
    public String xslPage(Model model) {
        model.addAttribute("users", xmlService.getAllUsers());
        return "xsl-page";
    }

    @GetMapping("/xsl/recipes")
    public void showXslRecipes(
            HttpServletResponse response,
            @RequestParam(required = false) String userName
    ) throws Exception {

        String xmlPath = "src/main/resources/xml/recipes.xml";
        String xslPath = "src/main/resources/xml/recipes.xsl";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(xmlPath));

        TransformerFactory tf = TransformerFactory.newInstance();
        StreamSource xsl = new StreamSource(new File(xslPath));
        Transformer transformer = tf.newTransformer(xsl);

        AppUser selectedUser = null;

        if (userName != null) {
            selectedUser = xmlService.getAllUsers().stream()
                    .filter(u -> (u.getFirstName() + " " + u.getLastName()).equals(userName))
                    .findFirst()
                    .orElse(null);
        }

        if (selectedUser == null) {
            selectedUser = xmlService.getFirstUser();
        }

        transformer.setParameter("userSkill", selectedUser.getSkillLevel());

        transformer.transform(
                new DOMSource(doc),
                new StreamResult(response.getWriter())
        );
    }

    @GetMapping("/users")
    public String allUsers(Model model) {
        List<AppUser> users = xmlService.getAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/users/add")
    public String showAddUser() {
        return "add-user";
    }

    @PostMapping("/users/delete/{key}")
    public String deleteUser(@PathVariable String key) {
        xmlService.deleteUser(key);
        return "redirect:/users";
    }
    @PostMapping("/users/add")
    public String addUser(AppUser user, Model model) {

        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()
                || user.getLastName() == null || user.getLastName().trim().isEmpty()
                || user.getSkillLevel() == null || user.getSkillLevel().trim().isEmpty()
                || user.getPreferredCuisineType() == null || user.getPreferredCuisineType().trim().isEmpty()) {

            model.addAttribute("error", "All fields are required!");
            return "add-user";
        }

        xmlService.saveUser(user);
        return "redirect:/users";
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


