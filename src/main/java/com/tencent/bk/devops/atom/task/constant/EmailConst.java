package com.tencent.bk.devops.atom.task.constant;

public interface EmailConst {

    String TEXT_FORMAT = "Text";

    String HTML_FORMAT = "Html";

    /**
     * 发送人邮箱
     */
    String SENDER_EMAIL = "sendEmail";

    /**
     * smtp域名如：smtp.qq.com
     */
    String SMTP_HOST = "smtpHost";

    /**
     * smtp用户
     */
    String SMTP_USER = "smtpUser";

    /**
     * smtp密码
     */
    String SMTP_PWD = "smtpPwd";

    /**
     * smtp port
     */
    String SMTP_PORT = "smtpPort";

    /**
     * 最大文件大小
     **/
    long MAX_CONTEXT_FILE_SIZE = 10 * 1024;
}
