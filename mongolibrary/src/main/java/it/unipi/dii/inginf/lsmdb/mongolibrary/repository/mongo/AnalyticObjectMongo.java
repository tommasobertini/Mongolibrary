package it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo;

import org.bson.Document;

public class AnalyticObjectMongo {

    private String key;
    private String value;

    public AnalyticObjectMongo(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    public AnalyticObjectMongo(Document analyticsDocument)
    {
        this(analyticsDocument.getString("key"), analyticsDocument.getString("value"));
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String display()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("key ").append(key).append(", ").append("value ").append(value);
        return sb.toString();
    }

}
