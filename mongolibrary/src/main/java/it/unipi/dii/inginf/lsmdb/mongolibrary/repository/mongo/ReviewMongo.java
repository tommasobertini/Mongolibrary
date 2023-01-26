package it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo;

import it.unipi.dii.inginf.lsmdb.mongolibrary.util.DateConverter;
import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;

public class ReviewMongo {
    private String bookTitle;
    private String username;
    private Integer score;
    private LocalDate reviewTime; //We need to convert from long in db to date on java (because of dataset)
    private String description;
    private ArrayList<String> likes;

    public ReviewMongo() //DEFAULT
    {
        this.bookTitle = "";
        this.username = "";
        this.score = 0;
        this.reviewTime = DateConverter.toLocalDate(0); //Conversion
        this.description = "";
        this.likes = new ArrayList<>();
    }

    public ReviewMongo(String bookTitle, String username, Integer score, Integer reviewTime, String description, ArrayList<String> likes)
    {
        this.bookTitle = bookTitle;
        this.username = username;
        this.score = score;
        this.reviewTime = DateConverter.toLocalDate(reviewTime); //Conversion
        this.description = description;
        this.likes = likes;
    }

    public ReviewMongo(Document reviewDocument)
    {
        this(reviewDocument.getString("bookTitle"),
                reviewDocument.getString("username"),
                reviewDocument.getInteger("score"),
                reviewDocument.getInteger("reviewTime"),
                reviewDocument.getString("description"),
                reviewDocument.get("likes", ArrayList.class)
        );
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getUsername() {
        return username;
    }

    public Integer getScore() {
        return score;
    }

    public LocalDate getReviewTime() {
        return reviewTime;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getLikes()
    {
        return likes;
    }

    public void setReviewScore(Integer score) {
        this.score = score;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }

    public int numberOfLikes()
    {
        return likes.size();
    }

    public String fullDisplay()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Username: ").append(username).append(System.getProperty("line.separator"))
                .append("Description: ").append(description).append(System.getProperty("line.separator"))
                .append("Book: ").append(bookTitle).append(System.getProperty("line.separator"))
                .append("Score: ").append(score).append(System.getProperty("line.separator"))
                .append("ReviewTime: ").append(reviewTime).append(System.getProperty("line.separator"))
                .append("Likes: ").append(numberOfLikes()).append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
