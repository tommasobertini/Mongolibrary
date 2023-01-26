package it.unipi.dii.inginf.lsmdb.mongolibrary;


import it.unipi.dii.inginf.lsmdb.mongolibrary.util.CustomBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MongolibraryApplication{

    private static CustomBean customBean = new CustomBean();

    public static CustomBean getCustomBean(){ return customBean;}


    public static void main(String[] args) {
        SpringApplication.run(MongolibraryApplication.class, args);
    }

}
