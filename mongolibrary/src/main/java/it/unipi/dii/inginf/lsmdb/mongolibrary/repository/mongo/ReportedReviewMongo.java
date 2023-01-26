package it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo;

import org.bson.Document;

public class ReportedReviewMongo {

    private String bookTitle;
    private String username;
    private Integer numberOfReports;
    private String description;

    public ReportedReviewMongo(String bookTitle, String username, Integer numberOfReports, String description) {
        this.bookTitle = bookTitle;
        this.username = username;
        this.numberOfReports = numberOfReports;
        this.description = description;
    }

    public ReportedReviewMongo(Document reviewDocument)
    {
        this(reviewDocument.get("reportedReviews", Document.class).getString("bookTitle"),
                reviewDocument.get("reportedReviews", Document.class).getString("username"),
                reviewDocument.getInteger("numberOfReports"),
                reviewDocument.get("reportedReviews", Document.class).getString("description")
        );
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getUsername() {
        return username;
    }

    public Integer getNumberOfReports() {
        return numberOfReports;
    }

    public String getDescription() {
        return description;
    }

    public String fullDisplay()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Username: ").append(username).append(System.getProperty("line.separator"))
                .append("Description: ").append(description).append(System.getProperty("line.separator"))
                .append("Book: ").append(bookTitle).append(System.getProperty("line.separator"))
                .append("Number of Reports: ").append(numberOfReports).append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
