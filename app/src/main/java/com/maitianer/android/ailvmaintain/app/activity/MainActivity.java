package com.maitianer.android.ailvmaintain.app.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bumptech.glide.Glide;
import com.guuguo.android.lib.app.LBaseActivitySupport;
import com.guuguo.android.lib.widget.roundview.RoundTextView;
import com.maitianer.android.ailvmaintain.R;
import com.maitianer.android.ailvmaintain.app.retrofit.ApiService;
import com.maitianer.android.ailvmaintain.app.retrofit.MyRetrofit;
import com.maitianer.android.ailvmaintain.app.ui.ErrorDialog;
import com.maitianer.android.ailvmaintain.app.ui.WalkingRouteOverlay;
import com.maitianer.android.ailvmaintain.entity.BaseResponse;
import com.maitianer.android.ailvmaintain.entity.Constant;
import com.maitianer.android.ailvmaintain.entity.MyApplication;
import com.maitianer.android.ailvmaintain.entity.NearCarModel;
import com.maitianer.android.ailvmaintain.entity.RkCCUDevice;
import com.rokyinfo.ble.AuthCodeCreator;
import com.rokyinfo.ble.toolbox.AuthCodeDeliverer;
import com.rokyinfo.ble.toolbox.protocol.model.AuthResult;
import com.rokyinfo.ble.toolbox.protocol.model.RemoteControlResult;
import com.rokyinfo.ble.toolbox.protocol.model.VehicleStatus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import rx.Subscriber;
import rx.functions.Action1;


/**
 * Crated by czq on 2017/9/20.
 */

public class MainActivity extends LBaseActivitySupport implements SensorEventListener, OnGetGeoCoderResultListener, OnGetRoutePlanResultListener {
    private String TAG = getClass().getSimpleName();
    private MapView mMapView;// 百度地图控件
    private BaiduMap bdMap;// 百度地图对象
    private LocationClient mLocClient;
    private Retrofit retrofit = MyRetrofit.getRetrofit(null);
    private ApiService apiService = retrofit.create(ApiService.class);
    private double mCurrentLat = 0.0;//当前纬度
    private double mCurrentLon = 0.0;//当前经度
    private double last_mapCenterLat = 0.0;//地图中心点的纬度
    private double last_mapCenterLng = 0.0;//地图中心点的经度
    private Double lastX = 0.0;
    private float mCurrentAccracy;
    private MyLocationData locData;
    private int mCurrentDirection;
    private ArrayList<Marker> car_Markers = new ArrayList<>();//电动车在地图上的集合
    private SensorManager mSensorManager;
    private PopupWindow mPopWindow;
    private ImageView topArrow;
    private ImageView underArrow;
    private Constant.State state = Constant.State.All;
    private static final int BAIDU_READ_PHONE_STATE = 100;
    private String[] PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    private LinearLayout car_information;
    private TextView tv_car_address;
    private TextView tv_car_code;
    private TextView tv_car_distance;
    private ImageView iv_car_power;
    private TextView tv_car_power_percentage;
    private TextView tv_car_position_distance;
    private RoundTextView setButton;
    private GeoCoder geoCoder;
    private Integer status;
    private Integer carId;
    private SharedPreferences sp;
    private LatLng setCarPosition;
    private Integer power;
    private int btn_state = 0;//
    private final int STATE_SCAN = 0;
    private final int STATE_POWERON = 1;//开启状态
    private final int STATE_POWEROFF = 2;//关闭状态
    TextView tv_btn_bottom;
    private int operating_status = 0;    //当前的操作状态 0默认 1上电 2断电
    private final int open_box = 21;
    private final int poweron = 22;
    private WalkingRouteOverlay walkingRouteOverlay;//步行的规划路线
    private RoutePlanSearch mSearch;
    private LatLng myPosition;
    private LatLng markerPosition;
    private boolean isFristLocation = true;

    public static void intentTo(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Nullable
    @Override
    public Toolbar getToolBar() {
        return (Toolbar) findViewById(R.id.id_tool_bar);
    }

    @NotNull
    @Override
    protected String getHeaderTitle() {
        return "全部车辆";
    }

    @Override
    protected boolean isNavigationBack() {
        return false;
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle("");
        ((TextView) findViewById(R.id.tv_title)).setText(title);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(MyApplication.INSTANCE);
        super.setContentView(layoutResID);
    }

    @Override
    protected void initView() {
        mMapView = (MapView) findViewById(R.id.bmapview);
        bdMap = mMapView.getMap();

        mMapView.removeViewAt(1);
        topArrow = (ImageView) findViewById(R.id.filter_top_arrow);
        underArrow = (ImageView) findViewById(R.id.filter_under_arrow);
        car_information = (LinearLayout) findViewById(R.id.car_information);
        tv_car_address = (TextView) findViewById(R.id.tv_caraddress_appointment_layout);
        tv_car_code = (TextView) findViewById(R.id.tv_code_appointment_layout);
        tv_car_distance = (TextView) findViewById(R.id.tv_distance_forecast_appointment_layout);
        iv_car_power = (ImageView) findViewById(R.id.img_battery_appointment_layout);
        tv_car_power_percentage = (TextView) findViewById(R.id.tv_battery_appointment_layout);
        tv_car_position_distance = (TextView) findViewById(R.id.tv_distance_appointment_layout);
        setButton = (RoundTextView) findViewById(R.id.btn_appoint_appointment_layout);
        tv_btn_bottom = (TextView) findViewById(R.id.tv_btn_bootom);

        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);

        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(this);

        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);//获取传感器管理服务
        bdMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        bdMap.setMyLocationEnabled(true);

        bdMap.getUiSettings().setOverlookingGesturesEnabled(false);//禁用俯视
        mMapView.showScaleControl(false);
        mMapView.showZoomControls(false);
        if (Build.VERSION.SDK_INT >= 23) {
            showContacts();
        } else {
            init();//init为定位方法
        }
    }

    protected void init() {

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(bdAbstractLocationListener);
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setOpenGps(true); // 打开gps
        locationClientOption.setCoorType("bd09ll"); // 设置坐标类型
        //    locationClientOption.setScanSpan(5000);//5秒刷新一次
        mLocClient.setLocOption(locationClientOption);
        mLocClient.start();
        bdMap.setMyLocationConfiguration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.FOLLOWING, true, null));
        setListener();
    }

    @Override
    protected int getBackExitStyle() {
        return getBACK_WAIT_TIME();
    }

    @Override
    protected void onResume() {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        //取消注册传感器监听
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocClient != null) {
            mLocClient.stop();
        }
        mSearch.destroy();
        // 关闭定位图层
        bdMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
    }

    @Override
    protected void loadData() {
        super.loadData();
    }

    private void showCarPosition(double lat, double lng, Integer id, Constant.State state) {
        BitmapDescriptor bitmap_car_ic;
        switch (state) {
            case Normal:
                bitmap_car_ic = BitmapDescriptorFactory.fromResource(R.drawable.main_car_ic);
                break;
            case lowPower:
                bitmap_car_ic = BitmapDescriptorFactory.fromResource(R.drawable.low_power_car);
                break;
            case Error:
                bitmap_car_ic = BitmapDescriptorFactory.fromResource(R.drawable.error_car);
                break;
            default:
                bitmap_car_ic = BitmapDescriptorFactory.fromResource(R.drawable.main_car_ic);
        }
        LatLng point = new LatLng(lat, lng);
        OverlayOptions option = new MarkerOptions()
                .position(point)
//                .animateType(MarkerOptions.MarkerAnimateType.jump)
                .icon(bitmap_car_ic);
        Marker marker = (Marker) bdMap.addOverlay(option);
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        marker.setExtraInfo(bundle);
//        marker.setTitle(code);
//        car_Markers.add(marker);
    }

    public void showContacts() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(getApplicationContext(), "没有权限,请手动开启定位权限", Toast.LENGTH_SHORT).show();
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(this, PERMISSIONS, BAIDU_READ_PHONE_STATE);
        } else {
            init();
        }
    }


    //Android6.0申请权限的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case BAIDU_READ_PHONE_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                    init();
                } else {
                    // 没有获取到权限，做特殊处理
                    Toast.makeText(getApplicationContext(), "获取位置权限失败，请手动开启", Toast.LENGTH_SHORT).show();
                    init();
                }
                break;
            default:
                break;
        }
    }

    protected void setListener() {
        bdMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                if (Math.abs(last_mapCenterLat - mapStatus.target.latitude) < 0.0025
                        && Math.abs(last_mapCenterLng - mapStatus.target.longitude) < 0.0025) {
                    return;
                }
                last_mapCenterLat = mapStatus.target.latitude;
                last_mapCenterLng = mapStatus.target.longitude;
                getNearCars(last_mapCenterLat, last_mapCenterLng, state);
            }
        });

        bdMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                myPosition = new LatLng(mCurrentLat, mCurrentLon);
                markerPosition = marker.getPosition();
                setCarPosition = marker.getPosition();
                Integer id = (Integer) marker.getExtraInfo().get("id");
                getCarById(id, markerPosition);
                tv_car_position_distance.setText(String.valueOf((int) DistanceUtil.getDistance(myPosition, markerPosition)));
                PlanNode stNode = PlanNode.withLocation(myPosition);
                PlanNode enNode = PlanNode.withLocation(markerPosition);
                mSearch.walkingSearch(new WalkingRoutePlanOption().from(stNode).to(enNode));
                return true;
            }
        });

        bdMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                car_information.setVisibility(View.GONE);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
    }

    public void getNearCars(double lat, double lng, final Constant.State state) {
        Integer carState;
        switch (state) {
            case All:
                carState = 0;
                break;
            case Normal:
                carState = 1;
                break;
            case Error:
                carState = 2;
                break;
            case lowPower:
                carState = 3;
                break;
            default:
                carState = 0;
        }
        apiService.getNearCars(lat, lng, carState)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<BaseResponse<List<NearCarModel>>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull BaseResponse<List<NearCarModel>> listBaseResponse) {
                        List<NearCarModel> nearCarModels = listBaseResponse.getData();
                        if (nearCarModels != null) {
                            if (state == Constant.State.All) {

                                for (NearCarModel model : nearCarModels) {
                                    if (model.getState() == 1 && model.getElectricity_quantity() > 1) {
                                        showCarPosition(model.getBaidu_lat(), model.getBaidu_lng(), model.getId(), Constant.State.Normal);
                                    } else if (model.getState() >= 2) {
                                        showCarPosition(model.getBaidu_lat(), model.getBaidu_lng(), model.getId(), Constant.State.Error);
                                    } else if (model.getElectricity_quantity() <= 1) {
                                        showCarPosition(model.getBaidu_lat(), model.getBaidu_lng(), model.getId(), Constant.State.lowPower);
                                    }
                                }
                            } else {
                                for (NearCarModel model : nearCarModels) {
                                    showCarPosition(model.getBaidu_lat(), model.getBaidu_lng(), model.getId(), state);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.i(TAG, "error");
                    }
                });
    }


    BDAbstractLocationListener bdAbstractLocationListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
//            last_mapCenterLat = location.getLatitude();
//            last_mapCenterLng = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            Log.d(TAG, "当前定位到位置: " + mCurrentLon + ";" + mCurrentLat);
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(mCurrentDirection)   // 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            bdMap.setMyLocationData(locData);
            MyLocationConfiguration configuration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null);
            bdMap.setMyLocationConfigeration(configuration);
            if (isFristLocation) {
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                bdMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                isFristLocation = false;
            }
            getNearCars(mCurrentLat, mCurrentLon, state);
        }

        @Override
        public void onLocDiagnosticMessage(int i, int i1, String s) {
            super.onLocDiagnosticMessage(i, i1, s);
            Log.d(TAG, i + ";" + i1 + ";" + s);
        }
    };

    //传感器
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection)
                    .latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            bdMap.setMyLocationData(locData);
//            Log.d(TAG, "系统位置: " + mCurrentLon + ";" + mCurrentLat);
        }
        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //我的位置
    public void locationOnClick(View view) {
        mLocClient.requestLocation();
        mLocClient.restart();
        if (locData == null) {
            return;
        }
        bdMap.setMyLocationData(locData);
        LatLng ll = new LatLng(locData.latitude, locData.longitude);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(ll).zoom(18.0f);
        bdMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    public void filterClick(View view) {
        if (mPopWindow == null) {
            topArrow.setVisibility(View.GONE);
            underArrow.setVisibility(View.VISIBLE);
            showPopupWindow();
        } else {
            if (mPopWindow.isShowing()) {
                topArrow.setVisibility(View.VISIBLE);
                underArrow.setVisibility(View.GONE);
                mPopWindow.dismiss();
            } else {
                topArrow.setVisibility(View.GONE);
                underArrow.setVisibility(View.VISIBLE);
                showPopupWindow();
            }
        }
        car_information.setVisibility(View.GONE);
    }

    private void showPopupWindow() {
        //设置contentView
        TextView textView = (TextView) findViewById(R.id.tv_title);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentview = inflater.inflate(R.layout.popup_filter, null);
        mPopWindow = new PopupWindow(contentview,
                435, 600);
        mPopWindow.setContentView(contentview);
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(w, h);
        mPopWindow.showAsDropDown(textView, -textView.getMeasuredWidth() / 2, 40);
        mPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopWindow.setOutsideTouchable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mPopWindow != null && mPopWindow.isShowing()) {
            mPopWindow.dismiss();
            mPopWindow = null;
        }
        topArrow.setVisibility(View.VISIBLE);
        underArrow.setVisibility(View.GONE);
        return super.onTouchEvent(event);
    }

    public void normalCarFilter(View view) {
        bdMap.clear();
        filterClick(view);
        getNearCars(mCurrentLat, mCurrentLon, Constant.State.Normal);
        ((TextView) findViewById(R.id.tv_title)).setText("正常车辆");
        state = Constant.State.Normal;
    }

    public void errorCarFilter(View view) {
        bdMap.clear();
        filterClick(view);
        getNearCars(mCurrentLat, mCurrentLon, Constant.State.Error);
        ((TextView) findViewById(R.id.tv_title)).setText("故障车辆");
        state = Constant.State.Error;
    }

    public void lowPowerCarFilter(View view) {
        bdMap.clear();
        filterClick(view);
        getNearCars(mCurrentLat, mCurrentLon, Constant.State.lowPower);
        ((TextView) findViewById(R.id.tv_title)).setText("低电车辆");
        state = Constant.State.lowPower;
    }

    public void allCarFilter(View view) {
        bdMap.clear();
        filterClick(view);
        getNearCars(mCurrentLat, mCurrentLon, Constant.State.All);
        ((TextView) findViewById(R.id.tv_title)).setText("全部车辆");
        state = Constant.State.All;
    }

    public void onSettingClick(View view) {
        LogoutActivity.intentTo(MainActivity.this);
    }

    private void getCarById(Integer id, final LatLng position) {
        apiService.getCarById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<BaseResponse<NearCarModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull BaseResponse<NearCarModel> nearCarModelBaseResponse) {
                        changeBtnState(STATE_SCAN);
                        NearCarModel nearCarModel = nearCarModelBaseResponse.getData();
                        int code = nearCarModelBaseResponse.getCode();
                        String msg = nearCarModelBaseResponse.getMsg();
                        status = nearCarModel.getState();
                        carId = nearCarModel.getId();
                        power = nearCarModel.getElectricity_quantity();
                        if (code == 200 && nearCarModel != null) {
                            tv_car_code.setText(nearCarModel.getCode());
                            showElectricityQuantity(nearCarModel.getElectricity_quantity());
                            if (nearCarModel.getState() < 2) {
                                setButton.getDelegate().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.dividers_color_dark));
                                setButton.setText("设为故障");
                            } else {
                                setButton.getDelegate().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.red));
                                setButton.setText("设为正常");
                            }
                            if (nearCarModel.getAddress() == null) {
                                geoCoder.reverseGeoCode(new ReverseGeoCodeOption()
                                        .location(position));
                            }
                            tv_car_address.setText(nearCarModel.getAddress());
                            car_information.setVisibility(View.VISIBLE);
                        } else {
                            MyApplication.INSTANCE.toast(msg, true);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        dialogDismiss();
                        dialogErrorShow("请求异常" + e.getMessage(), null, 400);
                    }
                });
    }

    private void showElectricityQuantity(int electricity_quantity) {
        tv_car_distance.setText(String.valueOf(electricity_quantity * 10));
        switch (electricity_quantity) {
            case 0:
                Glide.with(this).load(R.drawable.battery_empty).into(iv_car_power);
                tv_car_power_percentage.setText("0%");
                break;
            case 1:
                Glide.with(this).load(R.drawable.battery_1).into(iv_car_power);
                tv_car_power_percentage.setText("20%");
                break;
            case 2:
                Glide.with(this).load(R.drawable.battery_2).into(iv_car_power);
                tv_car_power_percentage.setText("40%");
                break;
            case 3:
                Glide.with(this).load(R.drawable.battery_2).into(iv_car_power);
                tv_car_power_percentage.setText("60%");
                break;
            case 4:
                Glide.with(this).load(R.drawable.battery_3).into(iv_car_power);
                tv_car_power_percentage.setText("80%");
                break;
            case 5:
                Glide.with(this).load(R.drawable.battery_full).into(iv_car_power);
                tv_car_power_percentage.setText("100%");
                break;
            default:
                Glide.with(this).load(R.drawable.battery_full).into(iv_car_power);
                tv_car_power_percentage.setText("100%");
                break;
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            return;
        }
        tv_car_address.setText(reverseGeoCodeResult.getAddress());
    }

    public void onSetStatusClick(View view) {
        if (status == 1) {
            final ErrorDialog errorDialog = new ErrorDialog(getActivity());
            errorDialog.show();
            errorDialog.setNoOnclickListener(null, new ErrorDialog.onNoOnclickListener() {
                @Override
                public void onNoClick() {
                    errorDialog.cancel();
                }
            });
            errorDialog.setYesOnclickListener(null, new ErrorDialog.onYesOnclickListener() {
                @Override
                public void onYesClick() {
                    String content = errorDialog.getMessage();
                    if (!TextUtils.isEmpty(content)) {
                        setError(carId, content);
                        errorDialog.dismiss();
                    } else {
                        MyApplication.INSTANCE.toast("故障说明不能为空", true);
                    }
                }
            });
        } else if (status == 2) {
            setNormal(carId);
        }
    }

    private void setNormal(Integer id) {
        dialogLoadingShow("加载中", false, 0, null);
        sp = getSharedPreferences("User", MODE_PRIVATE);
        if (sp != null) {
            String token = sp.getString("token", "");
            apiService.setNormal(id, token)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<BaseResponse>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull BaseResponse setModelBaseResponse) {
                            if (setModelBaseResponse.getCode() == 200) {
                                if (power <= 1) {
                                    showCarPosition(setCarPosition.latitude, setCarPosition.longitude, carId, Constant.State.lowPower);
                                    setButton.getDelegate().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.dividers_color_dark));
                                    setButton.setText("设为故障");
                                    status = 1;
                                } else {
                                    showCarPosition(setCarPosition.latitude, setCarPosition.longitude, carId, Constant.State.Normal);
                                    setButton.getDelegate().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.dividers_color_dark));
                                    setButton.setText("设为故障");
                                    status = 1;
                                }
                                dialogDismiss();
                            } else {
                                dialogDismiss();
                                MyApplication.INSTANCE.toast(setModelBaseResponse.getMsg(), true);
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            dialogDismiss();
                            dialogErrorShow("请求异常:" + e.getMessage(), null, 400);
                        }
                    });
        }
    }

    private void setError(Integer id, String content) {
        dialogLoadingShow("加载中", false, 0, null);
        sp = getSharedPreferences("User", MODE_PRIVATE);
        if (sp != null) {
            String token = sp.getString("token", "");
            apiService.setError(id, token, content)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<BaseResponse>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull BaseResponse setModelBaseResponse) {
                            if (setModelBaseResponse.getCode() == 200) {
                                showCarPosition(setCarPosition.latitude, setCarPosition.longitude, carId, Constant.State.Error);
                                setButton.getDelegate().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.red));
                                setButton.setText("设为正常");
                                status = 2;
                                dialogDismiss();
                            } else {
                                dialogDismiss();
                                MyApplication.INSTANCE.toast(setModelBaseResponse.getMsg(), true);
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            dialogDismiss();
                            dialogErrorShow("请求异常:" + e.getMessage(), null, 400);
                        }
                    });
        }
    }

    public void onCarInformationClick(View view) {
        CarInformationActivity.intentTo(this, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == Constant.REQUEST_SCAN) {
            Log.d("onActivityResult", "获取鉴权码成功，开启蓝牙成功");
            String key = data.getStringExtra("key");
            String macaddress = data.getStringExtra("macaddress");
            String sn = data.getStringExtra("sn");
            RkCCUDevice mRkCCUDevice = new RkCCUDevice();
            mRkCCUDevice.setSn(sn);
            mRkCCUDevice.setMacAddress(macaddress);
            mRkCCUDevice.setAuthCode(key);
            MyApplication.getInstance().setCurrentRkCCUDevice(mRkCCUDevice);
            setAuthCode();
            changeBtnState(STATE_POWEROFF);
//            tv_btn_bottom.setText(sn + macaddress);
        } else if (requestCode == Constant.REQUEST_ENABLE_BT) {
            //蓝牙成功开启
            MyApplication.INSTANCE.toast("蓝牙开启成功", true);
            if (1 == operating_status) {
                poweron();
            } else {
                poweroff();
            }
        } else if (requestCode == Constant.REQUEST_SCAN_ENABLE_BT) {
            //蓝牙开启成功，进入扫码页
            Log.d(TAG, "REQUEST_SCAN_ENABLE_BT");
            Intent intent = new Intent(getActivity(), ScanActivity.class);
            intent.putExtra("requestCode", Constant.REQUEST_SCAN);
            startActivityForResult(intent, Constant.REQUEST_SCAN);
        } else if (requestCode == Constant.REQUEST_ENABLE_BOX) {
            getCarStatus(open_box);
        }
    }

    //检查蓝牙是否开启
    public boolean checkBlueTooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    private void changeBtnState(int state) {
        btn_state = state;
        if (state == STATE_POWEROFF) {
            btn_state = STATE_POWEROFF;
            tv_btn_bottom.setText("启动电驴");
            tv_btn_bottom.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.bottom_poweron), null, null, null);
        } else if (state == STATE_POWERON) {
            btn_state = STATE_POWERON;
            tv_btn_bottom.setText("关闭电驴");
            tv_btn_bottom.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.bottom_poweroff), null, null, null);
        } else if (state == STATE_SCAN) {
            btn_state = STATE_SCAN;
            tv_btn_bottom.setText("扫码用车");
            tv_btn_bottom.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.small_scan_bold), null, null, null);
        }
    }

    //扫码用车
    public void btnBottomOnClick(View view) {
        if (btn_state == STATE_SCAN) {
            scanOnClick();
        } else if (btn_state == STATE_POWEROFF) {
            operating_status = 1;
            if (!checkBlueTooth()) {
                Intent openBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(openBT, Constant.REQUEST_ENABLE_BT);
                return;
            }
            poweron();
        } else if (btn_state == STATE_POWERON) {
            new MaterialDialog.Builder(getActivity())
                    .title("提示")
                    .content("请问您确定要关闭电动车么")
                    .positiveText("立即关闭")
                    .neutralText("取消")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            operating_status = 2;
                            if (!checkBlueTooth()) {
                                Intent openBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(openBT, Constant.REQUEST_ENABLE_BT);
                                return;
                            }
                            poweroff();
                        }
                    })
                    .show();
        }
    }

    //点击扫码用车
    private void scanOnClick() {
        if (!checkBlueTooth()) {
            Intent openBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(openBT, Constant.REQUEST_SCAN_ENABLE_BT);
            return;
        }
        Intent intent = new Intent(getActivity(), ScanActivity.class);
        intent.putExtra("requestCode", Constant.REQUEST_SCAN);
        startActivityForResult(intent, Constant.REQUEST_SCAN);
    }

    private void poweron() {
        dialogLoadingShow("正在尝试开启电驴", false, 0, null);
        MyApplication.getInstance().getRkBluetoothClient().getRk4103ApiService().powerOn(
                MyApplication.getInstance().getCurrentRkCCUDevice().getMacAddress())
                .subscribeOn(rx.schedulers.Schedulers.io())
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RemoteControlResult>() {
                    @Override
                    public void onCompleted() {
                        operating_status = 0;
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialogDismiss();
                        dialogErrorShow("开启电驴失败", null, 400);
                        Log.e("poweron", "onError");
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(RemoteControlResult remoteControlResult) {
                        dialogDismiss();
                        if (remoteControlResult.isSuccess()) {
                            Log.d("poweron", "success");
                            changeBtnState(STATE_POWERON);
                            dialogCompleteShow("开启成功", null, 400);
                        }
                    }
                });
    }

    private void poweroff() {
        dialogLoadingShow("正在关闭电动车", false, 0, null);
        MyApplication.getInstance().getRkBluetoothClient().getRk4103ApiService().powerOff(
                MyApplication.getInstance().getCurrentRkCCUDevice().getMacAddress())
                .subscribeOn(rx.schedulers.Schedulers.io())
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RemoteControlResult>() {
                    @Override
                    public void onCompleted() {
                        operating_status = 0;
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialogDismiss();
                        new MaterialDialog.Builder(getActivity())
                                .title("提示")
                                .content("电动车关闭失败，请您再次尝试关闭电动车后再进行还车操作")
                                .positiveText("知道了")
                                .show();

                        Log.e("poweroff", "error");
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(RemoteControlResult remoteControlResult) {
                        dialogDismiss();
                        if (remoteControlResult.isSuccess()) {
                            Log.d("poweroff", "success");
                            changeBtnState(STATE_POWEROFF);
                            dialogCompleteShow("关闭成功", null, 400);
                        }
                    }
                });
    }

    //点击开启座桶
    public void onOpenBoxClick(View view) {
        if (!checkBlueTooth()) {
            Intent openBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(openBT, Constant.REQUEST_ENABLE_BOX);
            return;
        }
        getCarStatus(open_box);
    }

    //用来判断是否连上车
    private void getCarStatus(final int operate) {
        if (MyApplication.getInstance().getCurrentRkCCUDevice() != null) {
            dialogLoadingShow("正在检测车辆", false, 0, null);
            MyApplication.getInstance().getRkBluetoothClient().getRk4103ApiService().getVehicleStatus(
                    MyApplication.getInstance().getCurrentRkCCUDevice().getMacAddress())
                    .subscribeOn(rx.schedulers.Schedulers.io())
                    .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<VehicleStatus>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.i(TAG, e.getMessage());
                            dialogDismiss();
                            dialogErrorShow("车辆连接失败，请重试", null, 400);
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(VehicleStatus vehicleStatus) {
                            if (operate == open_box) {
                                openBox();
                            } else if (operate == poweron) {
                                poweron();
                            }
                        }
                    });
        } else {
            MyApplication.INSTANCE.toast("请先扫码连接车辆", true);
        }
    }

    private void openBox() {
        if (MyApplication.getInstance().getCurrentRkCCUDevice() != null) {
            dialogLoadingShow("正在打开座桶", false, 0, null);
            MyApplication.getInstance().getRkBluetoothClient().getRk4103ApiService().openBox(MyApplication.getInstance().getCurrentRkCCUDevice().getMacAddress())
                    .subscribeOn(rx.schedulers.Schedulers.io())
                    .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<RemoteControlResult>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            dialogDismiss();
                            dialogErrorShow("打开座桶失败，请重试", null, 400);
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(RemoteControlResult remoteControlResult) {
                            dialogDismiss();
                            if (remoteControlResult.isSuccess()) {
                                dialogCompleteShow("打开座桶成功", null, 400);
                            }
                        }
                    });
        } else {
            MyApplication.INSTANCE.toast("请先扫码连接车辆", true);
        }
    }

    //设置鉴权码
    private void setAuthCode() {
        MyApplication.getInstance().getRkBluetoothClient().getRk4103ApiService().setAuthCodeCreator(new AuthCodeCreator() {
            @Override
            public void getAuthCode(AuthCodeDeliverer callBack) {
                obtainAuthCode(callBack);
                callBack.postAuthCode(MyApplication.getInstance().getCurrentRkCCUDevice().getAuthCode(), 0, null);
            }
        });
        MyApplication.getInstance().getRkBluetoothClient().observeAuthResult()
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(new Action1<AuthResult>() {
                    @Override
                    public void call(AuthResult authResult) {
                        //鉴权成功之后保存在本地
                        Log.d("setAuthCode", authResult.getResultDesc());
//                        if (authResult.isSuccess()) {
//                            ACache.get(mActivity).put(authResult.getMacAddress(), authResult.getAuthCodeStr());
//                            return;
//                        }
                    }
                });
    }

    private void obtainAuthCode(AuthCodeDeliverer callBack) {
        //查询本地存储的当前手机，如果已经连接过设备并鉴权成功则直接获取该鉴权码，本地获取不到则从平台接口获取鉴权码
     /*   String authCode = ACache.get(mActivity).getAsString(
                MyApplication.getInstance().getCurrentRkCCUDevice().getMacAddress());
        if (!TextUtils.isEmpty(authCode)) {
            callBack.postAuthCode(authCode, 0, null);
            return;
        }*/
        Log.d("obtainAuthCode", MyApplication.getInstance().getCurrentRkCCUDevice().getAuthCode());
        callBack.postAuthCode(MyApplication.getInstance().getCurrentRkCCUDevice().getAuthCode(), 0, null);
    }

    //步行导航 规划结果
    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
        //清除已有的规划路线
        if (walkingRouteOverlay != null) {
            walkingRouteOverlay.removeFromMap();
        }

        if (walkingRouteResult == null || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            return;
        }
        if (walkingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            return;
        }
        if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            if (walkingRouteResult.getRouteLines().size() >= 1) {
                walkingRouteOverlay = new WalkingRouteOverlay(bdMap);
                walkingRouteOverlay.setData(walkingRouteResult.getRouteLines().get(0));
                walkingRouteOverlay.addToMap();
                walkingRouteOverlay.zoomToSpan();
            }
        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }

}
