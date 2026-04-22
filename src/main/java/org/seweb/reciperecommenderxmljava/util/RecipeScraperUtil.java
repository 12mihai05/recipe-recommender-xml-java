package org.seweb.reciperecommenderxmljava.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seweb.reciperecommenderxmljava.model.Recipe;
import org.seweb.reciperecommenderxmljava.service.XmlService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RecipeScraperUtil {

	private static final String BBC_URL = "https://www.bbcgoodfood.com/recipes/collection/budget-autumn";

	private static final List<String> CUISINES = List.of(
			"Italian", "Asian", "Mexican", "French", "Romanian", "Indian", "American", "Mediterranean"
	);

	private static final List<String> DIFFICULTIES = List.of("Beginner", "Intermediate", "Advanced");

	private static final Set<String> INVALID_EXACT_TITLES = Set.of(
			"meal type",
			"diet type",
			"breakfast",
			"lunch",
			"dinner",
			"browse all meal types",
			"vegan"
	);

	private final Random random = new Random();
	private final XmlService xmlService;

	public RecipeScraperUtil(XmlService xmlService) {
		this.xmlService = xmlService;
	}

	public synchronized List<Recipe> getScrapedRecipes() {
		return xmlService.getAllRecipes();
	}

	public synchronized List<Recipe> getScrapedRecipes(int limit) {
		List<Recipe> allRecipes = xmlService.getAllRecipes();
		return allRecipes.stream().limit(limit).collect(Collectors.toList());
	}

	public synchronized int nextScrapedRecipeId() {
		return xmlService.generateNextRecipeId();
	}

	public synchronized void addScrapedRecipe(Recipe recipe) {
		List<Recipe> recipes = getScrapedRecipes();
		boolean alreadyExists = recipes.stream()
				.anyMatch(r -> normalize(r.getTitle()).equals(normalize(recipe.getTitle())));

		if (alreadyExists) {
			return;
		}

		recipe.setId(nextScrapedRecipeId());
		xmlService.saveRecipe(recipe);
	}

	public synchronized void deleteScrapedRecipe(int id) {
		xmlService.deleteRecipe(id);
	}

	public synchronized Recipe getScrapedRecipeById(int id) {
		return xmlService.getRecipeById(id);
	}

	public synchronized List<Recipe> getScrapedRecipesByCuisine(String cuisine) {
		return xmlService.getRecipesByCuisine(cuisine);
	}

	public synchronized List<Recipe> getScrapedRecipesBySkillAndCuisine(String skillLevel, String cuisine) {
		return xmlService.getRecipesBySkillAndCuisine(skillLevel, cuisine);
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}

	public synchronized int importBudgetAutumnRecipes(int limit) {
		try {
			Document doc = Jsoup.connect(BBC_URL).get();
			Elements recipeLinks = doc.select("a[href*='/recipes/']");
			int imported = 0;
			for (Element link : recipeLinks) {
				String title = link.text().trim();
				if (title.isEmpty() || INVALID_EXACT_TITLES.contains(normalize(title))) {
					continue;
				}
				String cuisine1 = CUISINES.get(random.nextInt(CUISINES.size()));
				String cuisine2 = CUISINES.get(random.nextInt(CUISINES.size()));
				String difficulty = DIFFICULTIES.get(random.nextInt(DIFFICULTIES.size()));
				Recipe recipe = new Recipe(0, title, cuisine1, cuisine2, difficulty);
				addScrapedRecipe(recipe);
				imported++;
				if (imported >= limit) break;
			}
			return imported;
		} catch (IOException e) {
			throw new RuntimeException("Failed to scrape recipes", e);
		}
	}
}
