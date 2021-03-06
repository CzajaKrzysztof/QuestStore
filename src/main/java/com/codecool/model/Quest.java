package com.codecool.model;

public class Quest {
    private int id;
    private String name;
    private String description;
    private int price;
    private String imageLink;
    private QuestCategoryEnum category;

    public Quest(int id, String name, String description, int price, String imageLink, QuestCategoryEnum category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageLink = imageLink;
        this.category = category;
    }

    public Quest(String name, String description, int price, QuestCategoryEnum category) {
        this.id = 0;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageLink = "/static/images/quest.jpg";
        this.category = category;
    }

    public Quest(int id, String name, String description, int price, QuestCategoryEnum category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageLink = "/static/images/quest.jpg";
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public QuestCategoryEnum getCategory() {
        return category;
    }

    public void setCategory(QuestCategoryEnum category) {
        this.category = category;
    }
}
