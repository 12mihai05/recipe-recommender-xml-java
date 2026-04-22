package org.seweb.reciperecommenderxmljava.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seweb.reciperecommenderxmljava.model.Recipe;
import org.seweb.reciperecommenderxmljava.service.XmlService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
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
	private static final int DEFAULT_SCRAPED_LIMIT = 40;

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
	private final List<Recipe> scrapedRecipesCache = new ArrayList<>();

	public int importBudgetAutumnRecipes(XmlService xmlService, int limit) {
		List<Recipe> scrapedRecipes = getScrapedRecipes(limit);
		if (scrapedRecipes.isEmpty()) {
			return 0;
		}

		Set<String> existingTitles = getExistingTitles(xmlService);
		int importedCount = 0;

		for (Recipe scrapedRecipe : scrapedRecipes) {
			String title = scrapedRecipe.getTitle();
			String normalizedTitle = normalize(title);
			if (normalizedTitle.isEmpty() || existingTitles.contains(normalizedTitle)) {
				continue;
			}

			Recipe recipe = new Recipe(
					xmlService.generateNextRecipeId(),
					title,
					scrapedRecipe.getCuisineType1(),
					scrapedRecipe.getCuisineType2(),
					scrapedRecipe.getDifficulty()
			);

			xmlService.saveRecipe(recipe);
			existingTitles.add(normalizedTitle);
			importedCount++;

			if (importedCount >= limit) {
				break;
			}
		}

		return importedCount;
	}

	public synchronized List<Recipe> getScrapedRecipes() {
		return getScrapedRecipes(DEFAULT_SCRAPED_LIMIT);
	}

	public synchronized List<Recipe> getScrapedRecipes(int limit) {
		if (scrapedRecipesCache.isEmpty()) {
			scrapedRecipesCache.addAll(buildScrapedRecipes(limit));
		}

		return scrapedRecipesCache.stream()
				.sorted(Comparator.comparingInt(Recipe::getId))
				.collect(Collectors.toList());
	}

	public synchronized int nextScrapedRecipeId() {
		return getScrapedRecipes().stream()
				.map(Recipe::getId)
				.max(Integer::compareTo)
				.orElse(0) + 1;
	}

	public synchronized void addScrapedRecipe(Recipe recipe) {
		List<Recipe> recipes = getScrapedRecipes();
		boolean alreadyExists = recipes.stream()
				.anyMatch(r -> normalize(r.getTitle()).equals(normalize(recipe.getTitle())));

		if (alreadyExists) {
			return;
		}

		recipe.setId(nextScrapedRecipeId());
		scrapedRecipesCache.add(recipe);
	}

	public synchronized void deleteScrapedRecipe(int id) {
		scrapedRecipesCache.removeIf(recipe -> recipe.getId() == id);
	}

	public synchronized Recipe getScrapedRecipeById(int id) {
		return getScrapedRecipes().stream()
				.filter(recipe -> recipe.getId() == id)
				.findFirst()
				.orElse(null);
	}

	public synchronized List<Recipe> getScrapedRecipesByCuisine(String cuisine) {
		String normalizedCuisine = normalize(cuisine);
		return getScrapedRecipes().stream()
				.filter(recipe -> normalize(recipe.getCuisineType1()).equals(normalizedCuisine)
						|| normalize(recipe.getCuisineType2()).equals(normalizedCuisine))
				.collect(Collectors.toList());
	}

	public synchronized List<Recipe> getScrapedRecipesBySkillAndCuisine(String skillLevel, String cuisine) {
		List<String> allowedDifficulties = getRecipeDifficultiesForSkillLevel(skillLevel);
		String normalizedCuisine = normalize(cuisine);

		return getScrapedRecipes().stream()
				.filter(recipe -> allowedDifficulties.contains(recipe.getDifficulty()))
				.filter(recipe -> normalize(recipe.getCuisineType1()).equals(normalizedCuisine)
						|| normalize(recipe.getCuisineType2()).equals(normalizedCuisine))
				.collect(Collectors.toList());
	}

	private List<Recipe> buildScrapedRecipes(int limit) {
		List<Recipe> recipes = new ArrayList<>();
		List<String> scrapedTitles = scrapeBudgetAutumnTitles();

		for (String title : scrapedTitles) {
			if (!isLikelyRecipeTitle(title)) {
				continue;
			}

			String cuisineType1 = randomCuisine();
			recipes.add(new Recipe(
					recipes.size() + 1,
					title,
					cuisineType1,
					randomSecondCuisine(cuisineType1),
					randomDifficulty()
			));

			if (recipes.size() >= limit) {
				break;
			}
		}

		return recipes;
	}

	public List<String> scrapeBudgetAutumnTitles() {
		Set<String> uniqueTitles = new HashSet<>();
		List<String> orderedTitles = new ArrayList<>();

		try {
			Document document = Jsoup.connect(BBC_URL)
					.userAgent("Mozilla/5.0")
					.timeout(10000)
					.get();

			// Try specific headline selector first, then fallback to links from recipe pages.
			collectTitles(document.select("h2[data-testid=card-headline]"), uniqueTitles, orderedTitles);
			collectTitles(document.select("a[href*=/recipes/collection/budget-autumn/]"), uniqueTitles, orderedTitles);
			collectTitles(document.select("a[href*=/recipes/]") , uniqueTitles, orderedTitles);
		} catch (IOException e) {
			System.err.println("Failed to scrape BBC Good Food titles: " + e.getMessage());
		}

		return orderedTitles;
	}

	private void collectTitles(Elements elements, Set<String> uniqueTitles, List<String> orderedTitles) {
		for (Element element : elements) {
			String title = element.text().trim();
			String normalized = normalize(title);

			if (normalized.isEmpty() || normalized.length() < 4) {
				continue;
			}

			if (uniqueTitles.add(normalized)) {
				orderedTitles.add(title);
			}
		}
	}

	private boolean isLikelyRecipeTitle(String title) {
		String normalized = normalize(title);
		if (normalized.isEmpty() || normalized.length() < 6) {
			return false;
		}

		if (INVALID_EXACT_TITLES.contains(normalized)) {
			return false;
		}

		if (normalized.endsWith(" recipes") || normalized.endsWith(" recipe")) {
			return false;
		}

		return normalized.split("\\s+").length >= 2;
	}

	private List<String> getRecipeDifficultiesForSkillLevel(String skillLevel) {
		List<String> difficulties = new ArrayList<>();
		difficulties.add("Beginner");

		if ("Intermediate".equals(skillLevel) || "Advanced".equals(skillLevel)) {
			difficulties.add("Intermediate");
		}

		if ("Advanced".equals(skillLevel)) {
			difficulties.add("Advanced");
		}

		return difficulties;
	}

	private Set<String> getExistingTitles(XmlService xmlService) {
		Set<String> existingTitles = new HashSet<>();
		for (Recipe recipe : xmlService.getAllRecipes()) {
			existingTitles.add(normalize(recipe.getTitle()));
		}
		return existingTitles;
	}

	private String randomCuisine() {
		return CUISINES.get(random.nextInt(CUISINES.size()));
	}

	private String randomSecondCuisine(String firstCuisine) {
		String second = firstCuisine;

		for (int i = 0; i < 5 && second.equals(firstCuisine); i++) {
			second = randomCuisine();
		}

		return second;
	}

	private String randomDifficulty() {
		return DIFFICULTIES.get(random.nextInt(DIFFICULTIES.size()));
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}
}



