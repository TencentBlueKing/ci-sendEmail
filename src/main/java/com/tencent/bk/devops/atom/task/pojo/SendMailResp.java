package com.tencent.bk.devops.atom.task.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * {"message": "邮件发送成功。", "code": 0, "data": null, "result": true, "request_id": "xxx"}
 */
@Data
public class SendMailResp implements Serializable {

    private Boolean result;

    @JsonProperty("request_id")
    private String requestId;

    private Integer code;

    private String message;

}
