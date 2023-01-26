package it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo;

import it.unipi.dii.inginf.lsmdb.mongolibrary.util.DateConverter;
import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;

public class BookMongo {
    private String title;
    private String description;
    private String authors;
    private String publisher;
    private LocalDate publishedDate;
    private String categories;
    private Double score;
    private Integer copies;
    private ArrayList<ReviewMongo> reviews = new ArrayList<>();

    public BookMongo(String title, String description,
                     String authors, String publisher,
                     Integer publishedDate, String categories,
                     Double score, Integer copies, ArrayList<Document> reviews)
    {
        this.title = title;
        this.description = description;
        this.authors = authors;
        this.publisher = publisher;
        this.publishedDate = DateConverter.toLocalDate(publishedDate);
        this.categories = categories;
        this.score = score;
        this.copies = copies;
        if(reviews != null)
            this.reviews = getEmbeddedReviews(reviews);
    }

    public BookMongo(Document bookDocument)
    {
        this(bookDocument.getString("Title"),
                bookDocument.getString("description"),
                bookDocument.getString("authors"),
                bookDocument.getString("publisher"),
                bookDocument.getInteger("publishedDate"),
                bookDocument.getString("categories"),
                bookDocument.getDouble("score"),
                bookDocument.getInteger("copies"),
                bookDocument.get("reviews", ArrayList.class)
            );
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthors() {
        return authors;
    }

    public String getCategories() {
        return categories;
    }

    public String getPublisher() {
        return publisher;
    }

    public Double getScore() {
        return score;
    }

    public Integer getCopies() {
        return copies;
    }

    public LocalDate getPublishDate() {
        return publishedDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public void setCopies(Integer copies) {
        this.copies = copies;
    }

    public ArrayList<ReviewMongo> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<ReviewMongo> reviews) {
        this.reviews = reviews;
    }

    private ArrayList<ReviewMongo> getEmbeddedReviews(ArrayList<Document> embeddedReviews)
    {
        ArrayList<ReviewMongo> retrievedReviews = new ArrayList<>();

        for(Document review : embeddedReviews)
            retrievedReviews.add(new ReviewMongo(review));

        return  retrievedReviews;
    }

    public String fullDisplay()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Title: ").append(title).append(System.getProperty("line.separator"))
                .append("Description: ").append(description).append(System.getProperty("line.separator"))
                .append("Authors: ").append(authors).append(System.getProperty("line.separator"))
                .append("Publisher: ").append(publisher).append(System.getProperty("line.separator"))
                .append("Publish Date: ").append(publishedDate).append(System.getProperty("line.separator"))
                .append("Categories: ").append(categories).append(System.getProperty("line.separator"))
                .append("Score: ").append(score).append(System.getProperty("line.separator"))
                .append("Available copies: ").append(copies).append(System.getProperty("line.separator"))
                .append("Reviews: ").append(System.getProperty("line.separator"));
        if(!reviews.isEmpty()) {
            for (ReviewMongo review : reviews)
                sb.append(review.fullDisplay());
        }
        return sb.toString();
    }

    public String simpleDisplay()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Title: ").append(title).append(System.getProperty("line.separator"))
                .append("Score: ").append(score).append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
