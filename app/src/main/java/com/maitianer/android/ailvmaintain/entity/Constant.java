package com.maitianer.android.ailvmaintain.entity;

/**
 * Created by czq on 2017/9/21.
 */

public class Constant {
    public static final String BASE_URL = "https://www.ailvdaibu.com";
    public static final int REQUEST_ENABLE_BT = 11;//开启蓝牙 请求码
    public static final int REQUEST_ENABLE_BOX = 15;//开启蓝牙 开启座桶请求码
    public static final int REQUEST_SCAN = 10;    //扫码用车 请求码
    public static final int REQUEST_SCAN_FEEDBACK = 12;    //用户反馈 扫码 请求码
    public static final int REQUEST_SCAN_ENABLE_BT = 13;    //用户反馈 扫码 请求码
    public static final Integer All = 0;
    public static final Integer Normal = 1;
    public static final Integer Error = 2;
    public static final Integer lowPower = 3;
    public enum State{
        All,
        Normal,
        Error,
        lowPower
    }
}
