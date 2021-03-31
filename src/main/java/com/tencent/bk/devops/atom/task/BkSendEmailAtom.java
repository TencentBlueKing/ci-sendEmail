package com.tencent.bk.devops.atom.task;

import com.tencent.bk.devops.atom.AtomContext;
import com.tencent.bk.devops.atom.common.Status;
import com.tencent.bk.devops.atom.pojo.AtomResult;
import com.tencent.bk.devops.atom.spi.AtomService;
import com.tencent.bk.devops.atom.spi.TaskAtom;
import com.tencent.bk.devops.atom.task.pojo.EmailParam;
import com.tencent.bk.devops.atom.task.pojo.SendMailReq;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static com.tencent.bk.devops.atom.task.constant.EmailConst.*;

@AtomService(paramClass = EmailParam.class)
public class BkSendEmailAtom implements TaskAtom<EmailParam> {
    private Logger logger = LoggerFactory.getLogger(BkSendEmailAtom.class);

    /**
     * 执行主入口
     *
     * @param atomContext 插件上下文
     */
    @Override
    public void execute(AtomContext<EmailParam> atomContext) {
        // 1.1 拿到请求参数
        EmailParam param = atomContext.getParam();
        logger.info("context param: {}", JsonUtil.toJson(param));
        // 1.2 拿到初始化好的返回结果对象
        AtomResult result = atomContext.getResult();
        // 2. 校验参数失败直接返回
        checkParam(param, result);
        if (result.getStatus() != Status.success) {
            return;
        }

        SendMailReq req = new SendMailReq();
        Map<String, String> bkSensitiveConfInfo = param.getBkSensitiveConfInfo();
        req.setSender(bkSensitiveConfInfo.get(SENDER_EMAIL));
        req.setTitle(param.getTitle());
        req.setReceiver(param.getReceivers());
        req.setCc(param.getCcs());
        req.setBody_format(param.getBodyFormat());

        try {
            req.setContent(getContext(param));
            sendEmail(req, bkSensitiveConfInfo);
            result.setMessage("Message delivery success...... ");
        } catch (Exception e) {
            result.setStatus(Status.failure);
            result.setMessage("Message sending Exception :" + e.getMessage());
        }
    }

    private void checkParam(EmailParam param, AtomResult result) {
        Map<String, String> bkSensitiveConfInfo = param.getBkSensitiveConfInfo();
        StringBuilder builder = new StringBuilder();
        if (bkSensitiveConfInfo.get(SMTP_HOST) == null) {
            builder.append("smtpHost cannot be empty ");
        }

        if (bkSensitiveConfInfo.get(SMTP_USER) == null) {
            builder.append("smtpUser cannot be empty ");
        }

        if (bkSensitiveConfInfo.get(SMTP_PWD) == null) {
            builder.append("smtpPwd cannot be empty ");
        }

        if (bkSensitiveConfInfo.get(SMTP_PORT) == null) {
            builder.append("smtpPort cannot be empty ");
        }

        if (bkSensitiveConfInfo.get(SENDER_EMAIL) == null) {
            builder.append("sendEmail cannot be empty ");
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
            logger.error(builder.toString());
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

    private void sendEmail(SendMailReq sendMailReq, Map<String, String> bkSensitiveConfInfo) {

        // 收件人电子邮箱
        String[] receivers = sendMailReq.getReceiver().split(",");
        InternetAddress to_address[] = new InternetAddress[receivers.length];
        try {
            for (int i = 0; i < receivers.length; i++) {
                to_address[i] = new InternetAddress(receivers[i]);
                logger.info("send mail to : {}", receivers[i]);
            }
        } catch (AddressException ae) {
            logger.warn("add receiver fail: {}", ae);
        }

        // 发件人电子邮箱
        String from = sendMailReq.getSender();

        // 指定发送邮件的主机为 localhost
        String host = bkSensitiveConfInfo.get(SMTP_HOST);

        // 获取系统属性
        Properties properties = System.getProperties();

        // 设置邮件服务器
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", bkSensitiveConfInfo.get(SMTP_PORT)); // 主机端口号
        properties.setProperty("mail.smtp.auth", "true"); // 是否需要用户认证
        properties.setProperty("mail.smtp.starttls.enable", "true"); // 启用TLS加密

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(bkSensitiveConfInfo.get(SMTP_USER), bkSensitiveConfInfo.get(SMTP_PWD));
            }
        });

        // 创建默认的 MimeMessage 对象
        MimeMessage message = new MimeMessage(session);

        // Set From: 头部头字段
        try {
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, to_address);
            // Set Subject: 头部头字段
            message.setSubject(sendMailReq.getTitle());

            // 设置消息体
            message.setText(sendMailReq.getContent());

            // 发送消息
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
