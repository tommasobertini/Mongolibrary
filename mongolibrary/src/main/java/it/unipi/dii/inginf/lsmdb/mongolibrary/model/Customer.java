package it.unipi.dii.inginf.lsmdb.mongolibrary.model;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Document(collection = "customers")
public class Customer extends User{
    private String nationality;
    private int dateOfBirth;
    private boolean status;
    private List<Customer> followers;
    private List<Customer> following;
    private List<Review> likedReviews;
    private List<Book> readingList;
    private List<Book> borrowingList;

    public Customer(){
    }

    public Customer(String id, String username, String password, String fullName, String email,
                    String nationality, int dateOfBirth, boolean status/*, List<Customer> followers,
                    List<Customer> following, List<Review> likedReviews, List<Book> readingList, List<Book> borrowingList*/)
    {

        super(id, username, password, fullName, email);

        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
        this.followers = followers;
        this.following = following;
        this.likedReviews = likedReviews;
        this.readingList = readingList;
        this.borrowingList = borrowingList;
    }

    public String getNationality() {
        return nationality;
    }

    public int getDateOfBirth() {
        return dateOfBirth;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setDateOfBirth(int dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<Customer> getFollowers() {
        return followers;
    }

    public void setFollowers(List<Customer> followers) {
        this.followers = followers;
    }

    public List<Customer> getFollowing() {
        return following;
    }

    public void setFollowing(List<Customer> following) {
        this.following = following;
    }

    public List<Review> getLikedReviews() {
        return likedReviews;
    }

    public void setLikedReviews(List<Review> likedReviews) {
        this.likedReviews = likedReviews;
    }

    public List<Book> getReadingList() {
        return readingList;
    }

    public void setReadingList(List<Book> readingList) {
        this.readingList = readingList;
    }

    public List<Book> getBorrowingList() {
        return borrowingList;
    }

    public void setBorrowingList(List<Book> borrowingList) {
        this.borrowingList = borrowingList;
    }

    @Override
    public String toString(){
        return "User{" +
                "id='" + getId() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", password='" + getPassword() + '\'' +
                ", name='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", nationality='" + nationality + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}
