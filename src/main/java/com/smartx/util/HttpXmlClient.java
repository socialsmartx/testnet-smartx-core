package com.smartx.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.smartx.core.consensus.SatException;

import net.sf.json.JSONObject;

public class HttpXmlClient {
    private static Logger log = Logger.getLogger(HttpXmlClient.class);
    public static String post(String url, Map<String, String> params) throws UnsupportedEncodingException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        String body = null;
        log.info("create httppost:" + url);
        HttpPost post = postForm(url, params);
        body = invoke(httpclient, post);
        httpclient.getConnectionManager().shutdown();
        return body;
    }
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    public String get(String url, String data) throws ClientProtocolException, IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        String body = null;
        url += data;
        log.info("create httppost:" + url);
        HttpGet httpget = new HttpGet(url);
        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, "10");
        HttpResponse response = null;
        response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        InputStream Instream = entity.getContent();
        body = convertStreamToString(Instream);
        httpclient.getConnectionManager().shutdown();
        return body;
    }
    public static String get(String url) {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        String body = null;
        log.info("create httppost:" + url);
        HttpGet get = new HttpGet(url);
        body = invoke(httpclient, get);
        httpclient.getConnectionManager().shutdown();
        return body;
    }
    private static String invoke(DefaultHttpClient httpclient, HttpUriRequest httpost) {
        HttpResponse response = sendRequest(httpclient, httpost);
        String body = paseResponse(response);
        return body;
    }
    private static String paseResponse(HttpResponse response) {
        log.info("get response from http server..");
        HttpEntity entity = response.getEntity();
        log.info("response status: " + response.getStatusLine());
        String charset = EntityUtils.getContentCharSet(entity);
        log.info(charset);
        String body = null;
        try {
            body = EntityUtils.toString(entity);
            log.info(body);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body;
    }
    private static HttpResponse sendRequest(DefaultHttpClient httpclient, HttpUriRequest httpost) {
        log.info("execute post...");
        HttpResponse response = null;
        try {
            response = httpclient.execute(httpost);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
    private static HttpPost postForm(String url, Map<String, String> params) throws UnsupportedEncodingException {
        HttpPost httpost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            nvps.add(new BasicNameValuePair(key, params.get(key)));
        }
        httpost.setEntity(new UrlEncodedFormEntity(nvps));
        try {
            log.info("set utf-8 form entity to httppost");
            //httpost.setEntity(new UrlEncodedFormEntity(nvps,HTTP.UTF_8));  
            httpost.setEntity(new UrlEncodedFormEntity(nvps));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return httpost;
    }
    public static void test1() {
        Map<String, String> params = new HashMap<String, String>();
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        params.put("access_key", "8bbb1a8e-b4eaf601-d58565a6-b82c8");
        params.put("created", time);
        params.put("method", "get_account_info");
        String info = "access_key=8bbb1a8e-b4eaf601-d58565a6-b82c8&created=" + time + "&method=get_account_info&secret_key=" + "5be10da0-7c18e591-bf80ef13-11f99";
        String sign = MyUtil.Md5(info);
        log.info("info:" + info + " sign=" + sign);
        params.put("sign", sign);
        String xml = "";
        try {
            xml = HttpXmlClient.post("https://api.huobi.com/apiv2.php", params);
            // xml = HttpXmlClient.post("http://btckan.com/price", params);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.info("============================================");
        log.info(xml);
    }
    public static void test2() {
        String xml = "";
        xml = HttpXmlClient.get("http://api.huobi.com/staticmarket/ticker_btc_json.js");
        JSONObject oj = JSONObject.fromObject(xml);
        log.info("============================================");
        log.info(xml);
        xml = oj.getString("ticker");
        oj = JSONObject.fromObject(xml);
        log.info(oj.getString("last"));
    }
    public static void getOptionNum() throws SatException, Exception {
    }
    public static void testandroidhttp() {
        String strurl = "http://www.baidu.com";
        try {
            URL url = new URL(strurl);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(urlConn.getInputStream());
            BufferedReader buff = new BufferedReader(in);
            String result = "";
            String readline = "";
            while ((readline = buff.readLine()) != null) {
                result += readline;
            }
            in.close();
            urlConn.disconnect();
            System.out.println(result);
        } catch (IOException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public static void main(String[] args) {
        testandroidhttp();
        System.exit(0);
		
		/*
		TenpayConfiguration.getInstance().initialize("config\\common.properties");	
		FuncBase.m_DBConnet = new DBConnection();
		String username = TenpayConfiguration.getInstance().getString("USER_NAME"); 
		String passwd = TenpayConfiguration.getInstance().getString("PASSWD");
		String connection = TenpayConfiguration.getInstance().getString("CONNECTION");
		Server.iconectMax = Long.parseLong(TenpayConfiguration.getInstance().getString("CONNECTMAX"));
		log.info(connection +" " + username + " " + passwd);
		
		if (!FuncBase.m_DBConnet.CreateMysql(connection, username, passwd))
		{
			log.error("connection to database error!");
			System.exit(0);
		}
		
		for (;;)
		{
		
		try {
			
			getOptionNum();
			
			Thread.sleep(5*1000);
			
		} catch (CoinException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}
		*/
    }
}
