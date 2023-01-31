package com.tencent.bk.devops.atom.task.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tencent.bk.devops.atom.task.pojo.EmailParam;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.tencent.bk.devops.atom.task.constant.EmailConst.WHITE_LIST;

public class NotifyUtilsTest extends TestCase {

    public void test_domain_mix() {

        String user1 = "xxx@yyy.zzz";
        String user2NoInWL = "abc@d.e";
        String user3 = "a@b.c";
        String whiteList = "yyy.zzz;@b.c";
        StringBuilder err = new StringBuilder();
        List<String> expect = Lists.newArrayList(user2NoInWL);

        List<String> notInWL = getNotInWL(";", user1, user2NoInWL, user3, whiteList, err);
        System.out.println(err);
        Assert.assertEquals(expect, notInWL);

        err = new StringBuilder();
        notInWL = getNotInWL(",", user1, user2NoInWL, user3, whiteList, err);
        System.out.println(err);
        Assert.assertEquals(expect, notInWL);
    }

    public void test_no_domain() {

        String user1 = "xxx@yyy.zzz";
        String user2NoDomain = "abc";
        String user3NotInWL = "a@b.c";

        StringBuilder err = new StringBuilder();
        String whiteList = "yyy.zzz";
        List<String> expect = Lists.newArrayList(user2NoDomain, user3NotInWL);

        List<String> notInWL = getNotInWL(";", user1, user2NoDomain, user3NotInWL, whiteList, err);
        System.out.println(err);
        Assert.assertEquals(expect, notInWL);

        err = new StringBuilder();
        notInWL = getNotInWL(",", user1, user2NoDomain, user3NotInWL, whiteList, err);
        System.out.println(err);
        Assert.assertEquals(expect, notInWL);
    }

    public void test_all_pass() {

        String user1 = "xxx@yyy.zzz";
        String user2 = "abc";
        String user3 = "a@b.c";

        StringBuilder err = new StringBuilder();
        String whiteListAllPass = "*";
        List<String> empty = Collections.emptyList();

        List<String> notInWL = getNotInWL(";", user1, user2, user3, whiteListAllPass, err);
        System.out.println(err);
        Assert.assertEquals(empty, notInWL);

        err = new StringBuilder();
        notInWL = getNotInWL(",", user1, user2, user3, whiteListAllPass, err);
        System.out.println(err);
        Assert.assertEquals(empty, notInWL);
    }

    public void test_all_pass_mix() {

        String user1 = "xxx@yyy.zzz";
        String user2 = "abc";
        String user3 = "a@b.c";

        StringBuilder err = new StringBuilder();
        String whiteListAllPass = "*;yyy.zzz";
        List<String> empty = Collections.emptyList();

        List<String> notInWL = getNotInWL(";", user1, user2, user3, whiteListAllPass, err);
        System.out.println(err);
        Assert.assertEquals(empty, notInWL);

        err = new StringBuilder();
        notInWL = getNotInWL(",", user1, user2, user3, whiteListAllPass, err);
        System.out.println(err);
        Assert.assertEquals(empty, notInWL);
    }

    @NotNull
    private static List<String> getNotInWL(String delimit, String user1,
                                           String user2,
                                           String user3,
                                           String whiteList,
                                           StringBuilder err) {
        EmailParam param = new EmailParam();
        param.setReceivers(String.join(delimit, user1, user2, user3));
        HashMap<String, String> bkSensitiveConfInfo = Maps.newHashMap();

        bkSensitiveConfInfo.put(WHITE_LIST, whiteList);
        param.setBkSensitiveConfInfo(bkSensitiveConfInfo);
        return NotifyUtils.checkReceivers(param, err);
    }
}
