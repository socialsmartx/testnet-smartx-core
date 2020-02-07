package com.smartx.api;

import java.util.ArrayList;

import com.smartx.core.consensus.SatException;

public class SatSeedNode {
    private String seedUrl = "";
    public static ArrayList<String> urls = new ArrayList<String>();
    public static int G_SEEDNODE = 0;
    public String getUrlNode() {
        if (SatSeedNode.G_SEEDNODE == 1) {
            return "http://106.75.168.107:8001";
        }
        return "http://106.75.168.107:8002";
    }
    public void getSeedUrls(ArrayList<String> urls) throws SatException {
        urls.add("http://106.75.168.107:8001");    //server
        urls.add("http://127.0.0.1:8002");        //client
        this.urls.add("http://106.75.168.107:8001");
        this.urls.add("http://127.0.0.1:8002");
    }
    public static void initSeeds() {
    }
}
