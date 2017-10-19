package com.maitianer.android.ailvmaintain.entity;

/**
 * Created by JiangyeLin on 2017/4/28/0028.
 */

public class NearCarModel {

    /**
     * id : 5
     * code : a
     * carno : a
     * state : 4
     * yw_state : 0
     * client_id : 0
     * lng : null
     * lat : null
     * last_time : null
     * mileage : null
     * speed : null
     * voltage : null
     * electricity_current : null
     * turn : null
     * update_time : 2017-03-31 09:44:33
     * recharge_rate : null
     * battery_state : null
     * electricity_quantity : null
     * model : a
     * factory : a
     * created_date : null
     * signal_state : 1
     * isallot : 0
     * safe_no : null
     * juli : null
     */

    private int id;
    private String code;    //车辆编号
    private String carno;   //车牌号
    private int state;      //	车辆状态 0=待投放 1=正常 2=故障 3=维修 4=报废
    private int yw_state;   //	车辆业务状态 0=闲置 1=使用中 2=预约中
    private int client_id;
    private double lng; //经度
    private double lat; //纬度
    private String last_time;
    private long mileage;   //	里程
    private String speed;   //	速度
    private String voltage; //	电压
    private String electricity_current; //	电流
    private String turn;    //	转把值
    private String update_time;
    private String recharge_rate;   //	充电次数
    private String battery_state;   //  电池状态
    private int electricity_quantity;    //电量
    private String model;   //	车辆型号
    private String factory; //	生产厂家
    private String created_date;
    private String address; //当前车辆位置
    private String appoint_time;    //预约时间
    private int appoint_member_id;  //预约人id
    private int signal_state;
    private int isallot;
    private String safe_no; //	保险单号
    private String juli;    //	当前距离
    private double baidu_lat;//百度纬度
    private double baidu_lng;//百度经度

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCarno() {
        return carno;
    }

    public void setCarno(String carno) {
        this.carno = carno;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getYw_state() {
        return yw_state;
    }

    public void setYw_state(int yw_state) {
        this.yw_state = yw_state;
    }

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getLast_time() {
        return last_time;
    }

    public void setLast_time(String last_time) {
        this.last_time = last_time;
    }

    public long getMileage() {
        return mileage;
    }

    public void setMileage(long mileage) {
        this.mileage = mileage;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getVoltage() {
        return voltage;
    }

    public void setVoltage(String voltage) {
        this.voltage = voltage;
    }

    public String getElectricity_current() {
        return electricity_current;
    }

    public void setElectricity_current(String electricity_current) {
        this.electricity_current = electricity_current;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getRecharge_rate() {
        return recharge_rate;
    }

    public void setRecharge_rate(String recharge_rate) {
        this.recharge_rate = recharge_rate;
    }

    public String getBattery_state() {
        return battery_state;
    }

    public void setBattery_state(String battery_state) {
        this.battery_state = battery_state;
    }

    public int getElectricity_quantity() {
        return electricity_quantity;
    }

    public void setElectricity_quantity(int electricity_quantity) {
        this.electricity_quantity = electricity_quantity;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAppoint_time() {
        return appoint_time;
    }

    public void setAppoint_time(String appoint_time) {
        this.appoint_time = appoint_time;
    }

    public int getAppoint_member_id() {
        return appoint_member_id;
    }

    public void setAppoint_member_id(int appoint_member_id) {
        this.appoint_member_id = appoint_member_id;
    }

    public int getSignal_state() {
        return signal_state;
    }

    public void setSignal_state(int signal_state) {
        this.signal_state = signal_state;
    }

    public int getIsallot() {
        return isallot;
    }

    public void setIsallot(int isallot) {
        this.isallot = isallot;
    }

    public String getSafe_no() {
        return safe_no;
    }

    public void setSafe_no(String safe_no) {
        this.safe_no = safe_no;
    }

    public String getJuli() {
        return juli;
    }

    public void setJuli(String juli) {
        this.juli = juli;
    }

    public double getBaidu_lat() {
        return baidu_lat;
    }

    public void setBaidu_lat(double baidu_lat) {
        this.baidu_lat = baidu_lat;
    }

    public double getBaidu_lng() {
        return baidu_lng;
    }

    public void setBaidu_lng(double baidu_lng) {
        this.baidu_lng = baidu_lng;
    }
}
