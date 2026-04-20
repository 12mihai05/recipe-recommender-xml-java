package org.seweb.reciperecommenderxmljava.model;

import jakarta.validation.constraints.NotBlank;

public class Recipe {

    private int id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "First cuisine type is required")
    private String cuisineType1;

    @NotBlank(message = "Second cuisine type is required")
    private String cuisineType2;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    public Recipe() {
    }

    public Recipe(int id, String title, String cuisineType1, String cuisineType2, String difficulty) {
        this.id = id;
        this.title = title;
        this.cuisineType1 = cuisineType1;
        this.cuisineType2 = cuisineType2;
        this.difficulty = difficulty;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCuisineType1() {
        return cuisineType1;
    }

    public void setCuisineType1(String cuisineType1) {
        this.cuisineType1 = cuisineType1;
    }

    public String getCuisineType2() {
        return cuisineType2;
    }

    public void setCuisineType2(String cuisineType2) {
        this.cuisineType2 = cuisineType2;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}

