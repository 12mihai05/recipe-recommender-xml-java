package org.seweb.reciperecommenderxmljava.util;

import jakarta.annotation.Nullable;
import org.seweb.reciperecommenderxmljava.service.XmlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RecipeScraperRunner implements CommandLineRunner {

    private final XmlService xmlService;
    private final RecipeScraperUtil recipeScraperUtil;

    @Value("${app.scraper.enabled:false}")
    private boolean scraperEnabled;

    public RecipeScraperRunner(XmlService xmlService, RecipeScraperUtil recipeScraperUtil) {
        this.xmlService = xmlService;
        this.recipeScraperUtil = recipeScraperUtil;
    }

    @Override
    public void run(@Nullable String... args) {
        if (!scraperEnabled) {
            return;
        }

        try {
            int imported = recipeScraperUtil.importBudgetAutumnRecipes(20);
            boolean valid = xmlService.validateRecipesXml();

            System.out.println("Scraper imported " + imported + " new recipes.");
            System.out.println("recipes.xml valid against XSD: " + valid);
        } catch (Exception e) {
            System.err.println("Scraper failed: " + e.getMessage());
        }
    }
}
