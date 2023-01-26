package it.unipi.dii.inginf.lsmdb.mongolibrary.model;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Document(collection = "admins")
public class Admin extends User{
    //private List<Review> reportedReview;

    public Admin(){
    }

    public Admin(String id, String username, String password, String fullName, String email/*, List<Review> reportedReview*/){

        super(id, username, password, fullName, email);
        //this.reportedReview = reportedReview;
    }

    /*public List<Review> getReportedReview() {
        return reportedReview;
    }

    public void setReportedReview(List<Review> reportedReview) {
        this.reportedReview = reportedReview;
    }*/

    /*
     *   Ban/unban of the user
     *   @param user to ban/unban
     */
    private void banUnban(Customer customer){
        customer.setStatus(!customer.getStatus());

    }
}
