package com.maitianer.android.ailvmaintain.entity;

/**
 * Created by JiangyeLin on 2017/5/8/0008.
 */

public class MacAddressModel {

    /**
     * key : L39qaE6lFAy+u+lsWKXX4A==
     * mac1 : 00:27:15:09:9f:4b
     * mac2 : c0:27:15:09:d5:e3
     */

    private String key;     //鉴权码
    private String mac1;    //ios 连接使用
    private String mac2;    //android 连接使用

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMac1() {
        return mac1;
    }

    public void setMac1(String mac1) {
        this.mac1 = mac1;
    }

    public String getMac2() {
        return mac2;
    }

    public void setMac2(String mac2) {
        this.mac2 = mac2;
    }
}
