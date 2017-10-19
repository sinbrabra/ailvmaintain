package com.maitianer.android.ailvmaintain.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guuguo.android.lib.app.LBaseActivitySupport;
import com.maitianer.android.ailvmaintain.R;
import com.maitianer.android.ailvmaintain.app.adapter.CarInformationAdapter;
import com.maitianer.android.ailvmaintain.app.retrofit.ApiService;
import com.maitianer.android.ailvmaintain.app.retrofit.MyRetrofit;
import com.maitianer.android.ailvmaintain.entity.BaseResponse;
import com.maitianer.android.ailvmaintain.entity.CarInformation;
import com.maitianer.android.ailvmaintain.entity.MyApplication;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

/**
 * Created by czq on 2017/10/10.
 */

public class CarInformationActivity extends LBaseActivitySupport {

    private TextView explanation;
    private int id;
    private static String ID;
    private Retrofit retrofit = MyRetrofit.getRetrofit(null);
    private ApiService apiService = retrofit.create(ApiService.class);
    private List<Map<String, Integer>> mapList = new ArrayList<Map<String, Integer>>();
    private RecyclerView mRecyclerView;
    private CarInformationAdapter mAdapter;

    public static void intentTo(Activity activity, Integer id) {
        Intent intent = new Intent(activity, CarInformationActivity.class);
        intent.putExtra(ID, id);
        activity.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_error_information;
    }

    @Nullable
    @Override
    public Toolbar getToolBar() {
        return (Toolbar) findViewById(R.id.id_tool_bar);
    }

    @NotNull
    @Override
    protected String getHeaderTitle() {
        return "车辆状态";
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle("");
        ((TextView) findViewById(R.id.tv_title)).setText(title);
    }

    @Override
    protected void initView() {
        super.initView();
        getToolBar().setNavigationIcon(R.drawable.ic_white_left_back);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_item);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, null, 1, 1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        id = getIntent().getIntExtra(ID, 0);
    }

    @Override
    protected void loadData() {
        super.loadData();
        getInformation(id);
    }

    private void getInformation(Integer id) {
        apiService.getCarInformation(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<BaseResponse<CarInformation>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull BaseResponse<CarInformation> carInformationBaseResponse) {
                        CarInformation carInformation = carInformationBaseResponse.getData();
                        String content = carInformation.getContent();
                        View view = getLayoutInflater().inflate(R.layout.rv_head, (ViewGroup) mRecyclerView.getParent(), false);
                        explanation =view.findViewById(R.id.explanation);
                        if (!TextUtils.isEmpty(content)) {
                            explanation.setText(content);
                        } else {
                            explanation.setText("暂无故障信息");
                        }
                        setAttribute(carInformation);
                        mAdapter = new CarInformationAdapter(mapList);
                        mAdapter.addHeaderView(view);
                        mRecyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        MyApplication.INSTANCE.toast(e.getMessage(), true);
                    }
                });
    }

    private void setAttribute(CarInformation carInformation) {

        Map<String, Integer> mMap = new HashMap<String, Integer>();
        mMap.put("电机控制器/电机类:MOS故障", carInformation.getMos());
        mMap.put("电机控制器/电机类:转把故障", carInformation.getTurnedHandle());
        mMap.put("电机控制器/电机类:缺相故障", carInformation.getPhase());
        mMap.put("电机控制器/电机类:刹把故障", carInformation.getHall());
        mMap.put("电机控制器/电机类:电机故障", carInformation.getElectric());
        mMap.put("电机控制器/电机类:刹把故障", carInformation.getBrakeHandle());
        mMap.put("电机控制器/电机类:ECU通信异常", carInformation.getEcuCommunication());
        mMap.put("中控类:GSENSOR故障", carInformation.getGsensor());
        mMap.put("中控类:BLE故障", carInformation.getBle());
        mMap.put("电池故障:BMS通信故障", carInformation.getBmsCommunication());
        mMap.put("电池故障:过充故障", carInformation.getOverCharge());
        mMap.put("电池故障:电芯高温故障", carInformation.getElecChipHighTemperature());
        mMap.put("电池故障:电芯低温故障", carInformation.getElecChipLowTemperature());
        mMap.put("电池故障:电池欠压", carInformation.getBatteryUnderVoltage());
        mMap.put("GPS故障:GPS通信故障", carInformation.getGpsCommunication());
        mMap.put("GPS故障:GSP信号异常", carInformation.getGpsSignalAnomaly());
        mMap.put("GPS故障:NO SIM", carInformation.getNoSim());
        mMap.put("GPS故障:外电断开", carInformation.getOutElecOff());
        mMap.put("其他:RS485通信故障", carInformation.getRs485());
        mMap.put("其他:RC异常", carInformation.getRcAnomaly());
        mMap.put("PCU故障:短路故障", carInformation.getShortCircuit());
        mMap.put("PCU故障:硬件故障", carInformation.getHardwareFault());
        mMap.put("PCU故障:DC使能控制", carInformation.getDcEnableControl());
        mMap.put("PCU故障:DC输出过压", carInformation.getDcOutputOverVoltage());
        mMap.put("PCU故障:DC输出欠压", carInformation.getDcOutputUnderVoltage());
        mMap.put("PCU故障:电源管理通信故障", carInformation.getPowerManagerCommunication());
        mMap.put("灯光断路故障:前左转", carInformation.getCbFrontLeftTurn());
        mMap.put("灯光断路故障:后左转", carInformation.getCbBackLeftTurn());
        mMap.put("灯光断路故障:前右转", carInformation.getCbFrontRightTurn());
        mMap.put("灯光断路故障:后右转", carInformation.getCbBackRightTurn());
        mMap.put("灯光断路故障:后右转", carInformation.getCbBackRightTurn());
        mMap.put("灯光断路故障:近光", carInformation.getCbNearLight());
        mMap.put("灯光断路故障:远光", carInformation.getCbBeam());
        mMap.put("灯光断路故障:尾灯", carInformation.getCbTaillight());
        mMap.put("灯光断路故障:刹车灯", carInformation.getCbBrakeLight());
        mMap.put("灯光断路故障:背景灯", carInformation.getCbBackgroundLight1());
        mMap.put("灯光断路故障:背景灯1", carInformation.getCbBackgroundLight1());
        mMap.put("灯光断路故障:背景灯2", carInformation.getCbBackgroundLight2());
        mMap.put("灯光断路故障:背景灯3", carInformation.getCbBackgroundLight3());
        mMap.put("灯光断路故障:喇叭故障 ", carInformation.getCbHornFault());
        mMap.put("灯光短路故障:前左转", carInformation.getScFrontLeftTurn());
        mMap.put("灯光短路故障:后左转", carInformation.getScBackLeftTurn());
        mMap.put("灯光短路故障:前右转", carInformation.getScFrontRightTurn());
        mMap.put("灯光短路故障:后右转", carInformation.getScBackRightTurn());
        mMap.put("灯光短路故障:近光", carInformation.getScNearLight());
        mMap.put("灯光短路故障:远光", carInformation.getScBeam());
        mMap.put("灯光短路故障:尾灯", carInformation.getScTaillight());
        mMap.put("灯光短路故障:刹车灯", carInformation.getScBrakeLight());
        mMap.put("灯光短路故障:背景灯1", carInformation.getScBackgroundLight1());
        mMap.put("灯光短路故障:背景灯2", carInformation.getScBackgroundLight2());
        mMap.put("灯光短路故障:背景灯3", carInformation.getScBackgroundLight3());
        mMap.put("灯光短路故障:喇叭故障", carInformation.getScHornFault());

        for(String key : mMap.keySet()){
            Map<String, Integer> map = new HashMap<String, Integer>();
            map.put(key, mMap.get(key));
            mapList.add(map);
        }
    }

}
