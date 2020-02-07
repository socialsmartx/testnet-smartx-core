package com.smartx.message;

import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Dictionary {
    public Map<String, String> args = new HashMap<String, String>();
    public Map<String, String> data = null;
    public static String ToJson(Dictionary dict) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(dict);
    }
    public static Dictionary FromJson(String dictstr) {
        try {
            Gson gson = new GsonBuilder().create();
            Dictionary dict = gson.fromJson(dictstr, new TypeToken<Dictionary>() {
            }.getType());
            return dict;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Dictionary() {
    }
    public Dictionary(String cmd) {
        if (args == null) args = new HashMap<String, String>();
        this.args.put("command", cmd);
    }
}
