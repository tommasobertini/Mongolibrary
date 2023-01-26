package it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo;

import it.unipi.dii.inginf.lsmdb.mongolibrary.model.Customer;
import it.unipi.dii.inginf.lsmdb.mongolibrary.model.User;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserMongo {

    private String username;
    private String email;

    public UserMongo() {} //DEFAULT

    public UserMongo(String username, String email)
    //Constructor from normal parameters
    {
        this.username = username;
        this.email = email;
    }

    public UserMongo(Document userDocument)
    //Constructor from document
    {
        this(userDocument.getString("username"),
                userDocument.getString("email")
        );
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String listDisplay()
    {
        return username;
    }

    public String fullDisplay()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Username: ").append(username).append(System.getProperty("line.separator"))
                .append("Email: ").append(email).append(System.getProperty("line.separator"));
        return sb.toString();
    }

    public String simpleDisplay()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Username: ").append(username).append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
