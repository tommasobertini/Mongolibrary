package it.unipi.dii.inginf.lsmdb.mongolibrary.util;

import java.util.HashMap;
import java.util.Map;

public class CustomBean {

    private static Map<String, Object> mapBean = new HashMap<String, Object>();

    public static void putBean(String key, Object o) {
        mapBean.put(key, o);
    }

    public static Object getBean(String key) {
        return mapBean.get(key);
    }

    public static void removeBean(String key){
        mapBean.remove(key);
    }

    public static void replaceBean(String key, Object o){
        mapBean.replace(key, o);
    }

    public static void clear(){
        mapBean.clear();
    }

    public CustomBean(){}



}
