package com.tencent.bk.devops.atom.task.utils;

import com.tencent.bk.devops.atom.task.pojo.SendMailReq;
import com.tencent.bk.devops.atom.task.pojo.SendMailResp;
import com.tencent.bk.devops.atom.utils.http.OkHttpUtils;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;

public class NotifyUtils {
    private final static String EMAIL_URL = "/api/c/compapi/cmsi/send_mail/";

    public static SendMailResp doPostRequest(String host, SendMailReq body) {

        String jsonBody = JsonUtil.toJson(body);
        String url = host + EMAIL_URL;
        System.out.printf("notify post url=%s|body=%s\n", url, JsonUtil.skipLogFields(body));

        String result = OkHttpUtils.doPost(url, jsonBody);
        // {"message": "邮件发送成功。", "code": 0, "data": null, "result": true, "request_id": "xxx"}
        System.out.printf("notify post request result=%s\n", result);

        return JsonUtil.fromJson(result, SendMailResp.class);
    }
}
