package com.smartx.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonNode {
    public void InitNode(List<String> cnn, String local) {
        for (int i = 0; i < cnn.size(); i++) {
            peers.add(cnn.get(i));
        }
        localnode = local;
    }
    public String localnode = "";
    public List<String> peers = Collections.synchronizedList(new ArrayList<String>());
}
