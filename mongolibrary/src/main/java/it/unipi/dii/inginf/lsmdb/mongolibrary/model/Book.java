package it.unipi.dii.inginf.lsmdb.mongolibrary.model;

import java.util.Date;
import java.util.List;

public class Book {
    private final String title;
    private final String description;
    private final String author;
    private final String image;
    // Preview ROBE:BOH
    private final String publisher;
    private final Date publish_data;
    private final List<String> category;
    private int rating;
    private int copies;

    public Book(String title, String description, String author, String image, String publisher, Date publish_data, List<String> category, int rating, int copies) {
        this.title = title;
        this.description = description;
        this.author = author;
        this.image = image;
        this.publisher = publisher;
        this.publish_data = publish_data;
        this.category = category;
        this.rating = rating;
        this.copies = copies;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getImage() {
        return image;
    }

    public String getPublisher() {
        return publisher;
    }

    public Date getPublish_data() {
        return publish_data;
    }

    public List<String> getCategory() {
        return category;
    }

    public int getRating() {
        return rating;
    }

    public int getCopies() {
        return copies;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }
}
