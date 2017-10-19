package com.maitianer.android.ailvmaintain.entity;

import com.guuguo.android.lib.BaseApplication;
import com.rokyinfo.ble.toolbox.RkBluetoothClient;

/**
 * Created by czq on 2017/9/20.
 */

public class MyApplication extends BaseApplication {

    private RkCCUDevice currentRkCCUDevice;
    private RkBluetoothClient rkBluetoothClient;

    @Override
    protected void init() {
        initBlueToothClient();
    }

    public RkCCUDevice getCurrentRkCCUDevice() {
        return currentRkCCUDevice;
    }

    public void setCurrentRkCCUDevice(RkCCUDevice currentRkCCUDevice) {
        this.currentRkCCUDevice = currentRkCCUDevice;
    }

    private void initBlueToothClient() {
        rkBluetoothClient = RkBluetoothClient.create(this);
        rkBluetoothClient.setQueueSize(1);
    }

    public RkBluetoothClient getRkBluetoothClient() {
        return rkBluetoothClient;
    }

    public static MyApplication  getInstance(){
        return (MyApplication)INSTANCE;
    }

}
