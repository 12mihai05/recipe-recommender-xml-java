package org.seweb.reciperecommenderxmljava.model;

import jakarta.validation.constraints.NotBlank;

public class AppUser {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Skill level is required")
    private String skillLevel;

    @NotBlank(message = "Preferred cuisine type is required")
    private String preferredCuisineType;

    public AppUser() {
    }

    public AppUser(String firstName, String lastName, String skillLevel, String preferredCuisineType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.skillLevel = skillLevel;
        this.preferredCuisineType = preferredCuisineType;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    public String getPreferredCuisineType() {
        return preferredCuisineType;
    }

    public void setPreferredCuisineType(String preferredCuisineType) {
        this.preferredCuisineType = preferredCuisineType;
    }
}


