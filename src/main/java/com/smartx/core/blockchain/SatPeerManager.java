package com.smartx.core.blockchain;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.smartx.api.SatSeedNode;
import com.smartx.api.v1.model.GetMineTaskResponse;
import com.smartx.config.SystemProperties;
import com.smartx.core.consensus.SatException;
import com.smartx.message.Message;
import com.smartx.util.HttpClientUtil;
import com.smartx.util.Tools;

public class SatPeerManager {
    public static final String HTTP_HEAD = "?";
    public SatSeedNode node = new SatSeedNode();
    private static final Logger logger = Logger.getLogger("SatPeerManager");
    public SystemProperties config = SystemProperties.getDefault();
    public String PutJsonCmd(String url, String json) {
        url += HTTP_HEAD;
        url += Tools.getURLEncoderString(json);
        return HttpClientUtil.httpClientCall(url, 5000, "utf-8");
    }
    public Message QueryMessageV1(String url, Message message) throws Exception {
        try {
            String content = "";
            Gson gson = new GsonBuilder().create();
            String json = Tools.getURLEncoderString(gson.toJson(message));
            url = "http://" + url;
            url += "/v1.0.0/getmine-task?json=";
            url += json;
            content = HttpClientUtil.httpClientCallException(url, 5000, "utf-8");
            GetMineTaskResponse response = gson.fromJson(content, new TypeToken<GetMineTaskResponse>() {
            }.getType());
            Message resp = Message.FromJson(Tools.getURLDecoderString(response.getResult().getJson()));
            if (resp != null && resp.args.get("ret").equals("0")) {
                return resp;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            //treturn null;
            throw e;
        }
        return null;
    }

    public Message QueryRegisterERC(String url, Message message) throws Exception {
        try {
            String content = "";
            Gson gson = new GsonBuilder().create();
            String json = Tools.getURLEncoderString(gson.toJson(message));
            url = "http://" + url;
            url += "/v1.0.0/registerERC?json=";
            url += json;
            content = HttpClientUtil.httpClientCallException(url, 5000, "utf-8");
            GetMineTaskResponse response = gson.fromJson(content, new TypeToken<GetMineTaskResponse>() {
            }.getType());
            Message resp = Message.FromJson(Tools.getURLDecoderString(response.getResult().getJson()));
            if (resp != null && resp.args.get("ret").equals("0")) {
                return resp;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            //treturn null;
            throw e;
        }
        return null;
    }

}
