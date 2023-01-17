package com.tencent.bk.devops.atom.task.utils;

import com.tencent.bk.devops.atom.task.pojo.SendMailReq;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.concurrent.TimeUnit;

public class NotifyUtils {
    private final static String EMAIL_URL = "/api/c/compapi/cmsi/send_mail/";

    private static final OkHttpClient client = new OkHttpClient
        .Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build();

    public static String doPostRequest(String host, SendMailReq body) throws Exception {

        String jsonBody = JsonUtil.toJson(body);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody);
        String url = host + EMAIL_URL;
        System.out.printf("notify post url=%s|body=%s\n", url, JsonUtil.skipLogFields(body));

        Request request = new Request.Builder().url(url).post(requestBody).build();
        String result = doRequest(request);
        System.out.printf("notify post request result=%s\n", result);

        return result;
    }

    private static String doRequest(Request request) throws Exception {
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return "true";
            } else {
                if (response.body() != null) {
                    return response.body().string();
                } else {
                    return null;
                }
            }
        }
    }
}
