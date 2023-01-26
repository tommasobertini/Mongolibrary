package it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo;

import org.bson.Document;

public class AnalyticObjectMongo {

    private String key;
    private String value;

    public AnalyticObjectMongo(Object key, Object value)
    {
        this.key = key.toString();
        this.value = value.toString();
    }

    public AnalyticObjectMongo(Document analyticsDocument, String value)
    {
        this(analyticsDocument.get("_id"), analyticsDocument.get(value));
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
