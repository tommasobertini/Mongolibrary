package it.unipi.dii.inginf.lsmdb.mongolibrary.model;

import java.util.Date;

public class Review {
    private final String title;
    private final String userId;
    private int likes;
    private int reviewScore;
    private final Date reviewTime;
    private String reviewSummary;
    private String reviewText;

    public Review(String title, String userId, int likes, int reviewScore, Date reviewTime, String reviewSummary, String reviewText) {
        this.title = title;
        this.userId = userId;
        this.likes = likes;
        this.reviewScore = reviewScore;
        this.reviewTime = reviewTime;
        this.reviewSummary = reviewSummary;
        this.reviewText = reviewText;
    }

    public String getTitle() {
        return title;
    }

    public String getUserId() {
        return userId;
    }

    public int getLikes() {
        return likes;
    }

    public int getReviewScore() {
        return reviewScore;
    }

    public Date getReviewTime() {
        return reviewTime;
    }

    public String getReviewSummary() {
        return reviewSummary;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewScore(int reviewScore) {
        this.reviewScore = reviewScore;
    }

    public void setReviewSummary(String reviewSummary) {
        this.reviewSummary = reviewSummary;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public void addLike(){
        this.likes = this.likes + 1;
    }

    public void subLike(){
        this.likes = this.likes + 1;
    }
}
