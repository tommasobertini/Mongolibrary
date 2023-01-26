package it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo;

import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;

public class CustomerMongo extends UserMongo {

    private String nationality;
    private Integer birthYear;
    private Boolean status;

    private ArrayList<String> readingList = new ArrayList<>();
    private ArrayList<String> borrowingList = new ArrayList<>();


    public CustomerMongo() {} //DEFAULT

    public CustomerMongo(String username, String email, String nationality,
                         Integer birthYear, Boolean status, ArrayList<String> readingList,
                         ArrayList<Document> borrowingList)
    {
        super(username, email);
        this.nationality = nationality;
        this.birthYear = birthYear;
        this.status = status;
        if(readingList != null)
            this.readingList = readingList;
        if(borrowingList != null)
            this.borrowingList = getEmbeddedNames(borrowingList);

    }

    public CustomerMongo(Document userDocument)
    {
        this(userDocument.getString("username"),
                userDocument.getString("email"),
                userDocument.getString("nationality"),
                userDocument.getInteger("birthYear"),
                userDocument.getBoolean("status"),
                userDocument.get("readingList", ArrayList.class),
                userDocument.get("borrowingList", ArrayList.class)
        );
    }

    public String getNationality() {
        return nationality;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public Boolean getStatus() {
        return status;
    }

    public String getStatusToString(){
        return (status) ? "Active" : "Banned";
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public ArrayList<String> getReadingList() {
        return readingList;
    }

    public ArrayList<String> getBorrowingList() {
        return borrowingList;
    }

    public void setReadingList(ArrayList<String> readingList) {
        this.readingList = readingList;
    }

    public void setBorrowingList(ArrayList<String> borrowingList) {
        this.borrowingList = borrowingList;
    }

    private ArrayList<String> getEmbeddedNames(ArrayList<Document> borrowedBook)
    {
        ArrayList<String> retrievedNames = new ArrayList<>();

        for(Document book : borrowedBook)
            retrievedNames.add(book.getString("booktitle"));

        return  retrievedNames;
    }

    public String fullDisplay()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.fullDisplay())
                .append("Nationality: ").append(nationality).append(System.getProperty("line.separator"))
                .append("Birthyear: ").append(birthYear).append(System.getProperty("line.separator"))
                .append("Status: ").append(status).append(System.getProperty("line.separator"))
                .append("Reading: ").append(System.getProperty("line.separator"));
        if(!readingList.isEmpty()) {
            for (String book : readingList)
                sb.append(book);
        }
        sb.append("Borrowing: ").append(System.getProperty("line.separator"));
        if(!borrowingList.isEmpty()) {
            for (String book : borrowingList)
                sb.append(book);
        }
        return sb.toString();
    }

    public String simpleDisplay()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.simpleDisplay())
                .append("Nationality: ").append(nationality).append(System.getProperty("line.separator"))
                .append("Birthyear: ").append(birthYear).append(System.getProperty("line.separator"))
                .append("Reading: ").append(System.getProperty("line.separator"));
        if(!readingList.isEmpty()) {
            for (String book : readingList)
                sb.append(book);
        }
        sb.append("Borrowing: ").append(System.getProperty("line.separator"));
        if(!borrowingList.isEmpty()) {
            for (String book : borrowingList)
                sb.append(book);
        }
        return sb.toString();
    }
}