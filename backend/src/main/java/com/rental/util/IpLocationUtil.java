package com.rental.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

@Component
public class IpLocationUtil {
    @Value("${amap.key}")
    private String amapKey;

    @Value("${amap.ip-location-url}")
    private String ipLocationUrl;

    public String getCityByIp(String ip) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url = ipLocationUrl + "?key=" + amapKey + "&ip=" + ip;
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String result = EntityUtils.toString(response.getEntity());
            JSONObject json = JSON.parseObject(result);
            if ("1".equals(json.getString("status"))) {
                return json.getString("city");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

