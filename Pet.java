package com.example.testsample;

public class Pet {
    private String name;
    private String breed;
    private String age;
    private String description;
    private String imageUrl;

    public Pet() {

    }

    public Pet(String name, String breed, String age, String description,String imageUrl) {
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public String getAge() {
        return age;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    @Override
    public String toString() {
        return "Pets:\n" +
                "Name: " + name + "\n" +
                "Age: " + age + "\n" +
                "Breed: " + breed + "\n" +
                "Description: " + description + "\n" +
                "Image URL: " + imageUrl;
    }
}

