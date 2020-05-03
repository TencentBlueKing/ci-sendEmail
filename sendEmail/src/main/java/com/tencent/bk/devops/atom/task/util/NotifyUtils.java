package com.tencent.bk.devops.atom.task.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.bk.devops.atom.task.pojo.SendMailReq;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class NotifyUtils {

    private static final Logger logger = LoggerFactory.getLogger(NotifyUtils.class);

    private final static String EMAIL_URL = "/api/c/compapi/cmsi/send_mail/";

    private static OkHttpClient client = new OkHttpClient
            .Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    public static String doPostRequest(String host, SendMailReq body) throws Exception {

        String jsonBody = new ObjectMapper().writeValueAsString(body);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody);
        String url = host + EMAIL_URL;
        logger.info("notify post url: {}, notify post body:{}", url, jsonBody);

        Request request = new Request.Builder().url(url).post(requestBody).build();
        String result = doRequest(request);
        logger.info("notify post request result:{}", request);

        return result;
    }

    private static String doRequest(Request request) throws Exception {
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return "true";
            } else {
                return response.body().string();
            }
        }
    }
}
