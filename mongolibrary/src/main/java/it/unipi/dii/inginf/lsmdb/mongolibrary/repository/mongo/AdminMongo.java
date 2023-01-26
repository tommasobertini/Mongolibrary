package it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo;

import org.bson.Document;

import java.util.ArrayList;

public class AdminMongo extends UserMongo {

    public AdminMongo() {} //DEFAULT

    public AdminMongo(String username, String email)
    {
        super(username, email);
    }

    public AdminMongo(Document userDocument)
    {
        this(userDocument.getString("username"),
                userDocument.getString("email")
        );
    }
}
