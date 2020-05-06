package com.tencent.bk.devops.atom.task.pojo;

import com.tencent.bk.devops.atom.pojo.AtomBaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 插件参数定义
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmailParam extends AtomBaseParam {
    /**接收人集合**/
    private String receivers;
    /**抄送人**/
    private String ccs;
    /**通知标题**/
    private String title;
    /**邮件格式**/
    private String bodyFormat;
    /**邮件内容**/
    private String content;
    /**邮件路径**/
    private String contentPath;
}
