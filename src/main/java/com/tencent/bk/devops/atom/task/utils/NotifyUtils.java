package com.tencent.bk.devops.atom.task.utils;

import com.google.common.collect.Sets;
import com.tencent.bk.devops.atom.task.pojo.EmailParam;
import com.tencent.bk.devops.atom.task.pojo.SendMailReq;
import com.tencent.bk.devops.atom.task.pojo.SendMailResp;
import com.tencent.bk.devops.atom.utils.http.OkHttpUtils;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tencent.bk.devops.atom.task.constant.EmailConst.WHITE_LIST;

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

    public static List<String> checkReceivers(EmailParam param, StringBuilder errorMessage) {

        if (StringUtils.isBlank(param.getReceivers())) {
            errorMessage.append("| receiver cannot be empty ");
            return Collections.emptyList();
        }

        String splitRegex = "[,;]";
        String domainPrefix = "@";

        String whiteList = param.getBkSensitiveConfInfo().get(WHITE_LIST);
        if (whiteList.isEmpty()) { // nothing skip
            System.err.printf("%s is not deploy! \n", WHITE_LIST);
            return Collections.emptyList();
        }
        Set<String> whiteListSet = Sets.newHashSet(whiteList.split(splitRegex)).stream()
            .filter(s -> !s.trim().isEmpty())
            .map(s -> {
                if (s.trim().startsWith(domainPrefix)) {
                    return s.trim();
                } else if (s.trim().equals("*")) {
                    return s.trim();
                } else {
                    return domainPrefix + s.trim();
                }
            })
            .collect(Collectors.toSet());

        System.out.printf("%s deploy: %s \n", WHITE_LIST, whiteListSet);

        // do not check any domain
        if (whiteListSet.contains("*")) {
            System.err.printf("WARNING：%s deploy: [*] allow to send to all domains!\n", WHITE_LIST);
            return Collections.emptyList();
        }

        List<String> notInWL = new ArrayList<>();

        for (
            String receiver : param.getReceivers().

            split(splitRegex)) {
            int domainPos = receiver.lastIndexOf(domainPrefix);
            if (domainPos > 0) { // xxx@yyy.zzz
                // check xxx@yyy.zzz and yyy.zzz
                if (!whiteListSet.contains(receiver) && !whiteListSet.contains(receiver.substring(domainPos))) {
                    notInWL.add(receiver);
                }
            } else {
                if (!whiteListSet.contains(receiver)) { // no domain
                    notInWL.add(receiver);
                }
            }
        }

        if (!notInWL.isEmpty()) {
            errorMessage.append(String.format("| receiver %s not in whitelist %s", notInWL, whiteListSet));
        }

        return notInWL;
    }
}
