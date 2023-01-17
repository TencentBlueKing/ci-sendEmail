package com.tencent.bk.devops.atom.task;

import com.tencent.bk.devops.atom.AtomContext;
import com.tencent.bk.devops.atom.common.Status;
import com.tencent.bk.devops.atom.pojo.AtomResult;
import com.tencent.bk.devops.atom.spi.AtomService;
import com.tencent.bk.devops.atom.spi.TaskAtom;
import com.tencent.bk.devops.atom.task.pojo.EmailParam;
import com.tencent.bk.devops.atom.task.pojo.SendMailReq;
import com.tencent.bk.devops.atom.task.pojo.SendMailResp;
import com.tencent.bk.devops.atom.task.utils.NotifyUtils;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.tencent.bk.devops.atom.task.constant.EmailConst.BK_APP_CODE;
import static com.tencent.bk.devops.atom.task.constant.EmailConst.BK_APP_SECRET;
import static com.tencent.bk.devops.atom.task.constant.EmailConst.BK_HOST;
import static com.tencent.bk.devops.atom.task.constant.EmailConst.BK_TOKEN;
import static com.tencent.bk.devops.atom.task.constant.EmailConst.BK_USERNAME;
import static com.tencent.bk.devops.atom.task.constant.EmailConst.HTML_FORMAT;
import static com.tencent.bk.devops.atom.task.constant.EmailConst.MAX_CONTEXT_FILE_SIZE;
import static com.tencent.bk.devops.atom.task.constant.EmailConst.SEND;
import static com.tencent.bk.devops.atom.task.constant.EmailConst.TEXT_FORMAT;

@AtomService(paramClass = EmailParam.class)
public class BkSendEmailAtom implements TaskAtom<EmailParam> {
    /**
     * 执行主入口
     *
     * @param atomContext 插件上下文
     */
    @Override
    public void execute(AtomContext<EmailParam> atomContext) {
        // 1.1 拿到请求参数
        EmailParam param = atomContext.getParam();
        System.out.printf("context param: %s\n", JsonUtil.toJson(param));
        // 1.2 拿到初始化好的返回结果对象
        AtomResult result = atomContext.getResult();
        // 2. 校验参数失败直接返回
        checkParam(param, result);
        if (result.getStatus() != Status.success) {
            return;
        }

        SendMailReq req = new SendMailReq();
        Map<String, String> bkSensitiveConfInfo = param.getBkSensitiveConfInfo();
        req.setSender(bkSensitiveConfInfo.get(SEND));
        req.setTitle(param.getTitle());
        req.setReceiver(param.getReceivers());
        req.setCc(param.getCcs());
        req.setBkAppCode(bkSensitiveConfInfo.get(BK_APP_CODE));
        req.setBkAppSecret(bkSensitiveConfInfo.get(BK_APP_SECRET));
        req.setBkUsername(bkSensitiveConfInfo.get(BK_USERNAME));
        req.setBody_format(param.getBodyFormat());

        try {
            req.setContent(getContext(param));
            SendMailResp sendMailResp = NotifyUtils.doPostRequest(bkSensitiveConfInfo.get(BK_HOST), req);
            if (sendMailResp.getResult()) {
                result.setStatus(Status.success);
                result.setMessage("Message delivery success...... ");
            } else {
                result.setStatus(Status.failure);
                result.setMessage("Message delivery failure: " + sendMailResp.getMessage());
            }
        } catch (Exception e) {
            result.setStatus(Status.failure);
            result.setMessage("Message sending Exception :" + e.getMessage());
        }
    }

    private void checkParam(EmailParam param, AtomResult result) {
        Map<String, String> bkSensitiveConfInfo = param.getBkSensitiveConfInfo();
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bkSensitiveConfInfo.get(BK_APP_CODE))) {
            builder.append("bk_app_code cannot be empty | ");
        }
        if (StringUtils.isBlank(bkSensitiveConfInfo.get(BK_APP_SECRET))) {
            builder.append("bk_app_secret cannot be empty | ");
        }
        if (StringUtils.isBlank(bkSensitiveConfInfo.get(BK_HOST))) {
            builder.append("bk_host cannot be empty | ");
        }
        if (StringUtils.isBlank(bkSensitiveConfInfo.get(BK_TOKEN))
                && StringUtils.isBlank(bkSensitiveConfInfo.get(BK_USERNAME))) {
            builder.append("bk_token or bk_username cannot be empty | ");
        }
        if (StringUtils.isBlank(param.getReceivers())) {
            builder.append("receiver cannot be empty ");
        }
        if (StringUtils.equals(param.getBodyFormat(), HTML_FORMAT)) {
            if (StringUtils.isBlank(param.getContentPath())) {
                builder.append("contextPath cannot be empty");
            } else {
                File contentFile = new File(param.getBkWorkspace(), param.getContentPath());
                if (!contentFile.exists()) {
                    builder.append("content file:[").append(contentFile).append("] does not exist | ");
                }
                if (!contentFile.isFile()) {
                    builder.append("content file:[").append(contentFile).append("] must be a file |");
                }
                if (contentFile.length() > MAX_CONTEXT_FILE_SIZE) {
                    builder.append("content file:[").append(contentFile).append("] greater than 10M | ");
                }
            }
        }
        if (builder.length() > 0) {
            result.setStatus(Status.failure);
            result.setMessage(builder.toString());
            System.err.println(builder);
        }
    }

    private String getContext(EmailParam param) throws IOException {
        if (StringUtils.equals(param.getBodyFormat(), TEXT_FORMAT)) {
            return param.getContent();
        } else {
            File contextFile = new File(param.getBkWorkspace(), param.getContentPath());
            return FileUtils.readFileToString(contextFile, "utf-8");
        }
    }
}
