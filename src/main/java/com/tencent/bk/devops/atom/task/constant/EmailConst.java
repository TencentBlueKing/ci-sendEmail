package com.tencent.bk.devops.atom.task.constant;

public interface EmailConst {

    String WHITE_LIST = "sendmail_whitelist";

    String BK_APP_CODE = "bk_app_code";

    String BK_APP_SECRET = "bk_app_secret";

    String BK_TOKEN = "bk_token";

    String BK_USERNAME = "bk_username";

    String BK_HOST = "bk_host";

    String SEND = "sender";

    String TEXT_FORMAT = "Text";

    String HTML_FORMAT = "Html";

    /**
     * 最大文件大小
     **/
    long MAX_CONTEXT_FILE_SIZE = 10 * 1024;
}
