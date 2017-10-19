package com.maitianer.android.ailvmaintain.app.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.guuguo.android.lib.app.LBaseActivitySupport;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.maitianer.android.ailvmaintain.R;
import com.maitianer.android.ailvmaintain.app.retrofit.ApiService;
import com.maitianer.android.ailvmaintain.app.retrofit.MyRetrofit;
import com.maitianer.android.ailvmaintain.entity.BaseResponse;
import com.maitianer.android.ailvmaintain.entity.Constant;
import com.maitianer.android.ailvmaintain.entity.MacAddressModel;
import com.maitianer.android.ailvmaintain.entity.MyApplication;

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

/**
 * Created by czq on 2017/10/11.
 */

public class ScanActivity extends LBaseActivitySupport{

    private Retrofit retrofit = MyRetrofit.getRetrofit(null);
    private ApiService apiService = retrofit.create(ApiService.class);
    private LinearLayout ll_code;
    private EditText et_code;
    private DecoratedBarcodeView barcodeView;

    private String TAG = "ScanActivity";
    private MacAddressModel macAddressModel;

    private String sn;
    private int requestCode;

    private BeepManager beepManager;

    private boolean isTorchOn = false;  //手电筒是否开启
    private String lastText;    //上次扫描的结果
    private String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private static final int BAIDU_READ_PHONE_STATE = 100;

    /**
     * Camera相机硬件操作类
     */
    private Camera camera = null;

    /**
     * Camera2相机硬件操作类
     */
    private CameraManager manager = null;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession = null;
    private CaptureRequest request = null;
    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private String cameraId = null;
    private boolean isSupportFlashCamera2 = false;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }
            lastText = result.getText();
            beepManager.playBeepSoundAndVibrate();
            onScanQRCodeSuccess(lastText);
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_scan;
    }

    @Nullable
    @Override
    public Toolbar getToolBar() {
        return (Toolbar) findViewById(R.id.id_tool_bar);
    }

    @NotNull
    @Override
    protected String getHeaderTitle() {
        return "扫码用车";
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle("");
        ((TextView) findViewById(R.id.tv_title)).setText(title);
    }

    @Override
    protected void initView() {
        super.initView();
        ll_code = (LinearLayout)findViewById(R.id.ll_code_scanactivity);
        et_code = (EditText)findViewById(R.id.et_code_scanactivity);
        barcodeView = (DecoratedBarcodeView)findViewById(R.id.zxing_barcode_scanner);
        showContacts();
    }

    @Override
    protected void loadData() {
        super.loadData();
        requestCode = getIntent().getIntExtra("requestCode", 0);
        this.setResult(RESULT_CANCELED);
        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(this);
        beepManager.setVibrateEnabled(true);
    }

    private void onScanQRCodeSuccess(String result) {
        Log.d("scan", result);
        int pos = result.indexOf(" ");
        if (pos <= 0) {
           MyApplication.INSTANCE.toast("二维码格式不正确", true);
            return;
        }
        sn = result.substring(0, pos).toUpperCase();
        if (TextUtils.isEmpty(sn)) {
            MyApplication.INSTANCE.toast("二维码格式不正确", true);
            return;
        }
        if (requestCode == Constant.REQUEST_SCAN_FEEDBACK) {
            Intent intent = new Intent();
            intent.putExtra("sn", sn);
            this.setResult(RESULT_OK, intent);
            this.finish();
            return;
        }
        getKeyByCode(sn);
    }

    public void showContacts() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(getApplicationContext(), "没有权限,请手动开启定位权限", Toast.LENGTH_SHORT).show();
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(this, PERMISSIONS, BAIDU_READ_PHONE_STATE);
        } else {
            initCamera();
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
                    // 获取到权限，作相应处理
                    initCamera();
                } else {
                    // 没有获取到权限，做特殊处理
                    Toast.makeText(getApplicationContext(), "获取相机权限失败，请手动开启", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    //手动输码 提交
    public void submitOnClick(View view) {
        sn = et_code.getText().toString();
        if (TextUtils.isEmpty(sn)) {
            MyApplication.INSTANCE.toast("请输入车辆编码", true);
            return;
        }
        if (requestCode == Constant.REQUEST_SCAN_FEEDBACK) {
            Intent intent = new Intent();
            intent.putExtra("sn", sn);
            this.setResult(RESULT_OK, intent);
            this.finish();
            return;
        }
        getKeyByCode(sn.toUpperCase());
    }

    /**
     * 初始化相机
     */
    public void initCamera() {
        this.manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        if (isLOLLIPOP()) {
            initCamera2();
        } else {
            camera = Camera.open();
        }
    }

    /**
     * 判断Android系统版本是否 >= LOLLIPOP(API21)
     *
     * @return boolean
     */
    private boolean isLOLLIPOP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 初始化Camera2
     */
    private void initCamera2() {
        new Object() {
            private void _initCamera2() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        for (String _cameraId : manager.getCameraIdList()) {
                            CameraCharacteristics characteristics = manager.getCameraCharacteristics(_cameraId);
                            // 过滤掉前置摄像头
                            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                                continue;
                            }
                            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                            if (map == null) {
                                continue;
                            }
                            cameraId = _cameraId;
                            // 判断设备是否支持闪光灯
                            isSupportFlashCamera2 = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        }._initCamera2();
    }

    /**
     * 开启闪光灯
     */
    public void turnOn() {
        if (!isSupportFlash()) {
            MyApplication.INSTANCE.toast("设备不支持闪光灯", true);
            return;
        }
        if (isTorchOn) {
            return;
        }
        if (isLOLLIPOP()) {
            turnLightOnCamera2();
        } else {
            turnLightOnCamera(camera);
        }
    }

    /**
     * 关闭闪光灯
     */
    public void turnOff() {
        if (!isSupportFlash()) {
            MyApplication.INSTANCE.toast("设备不支持闪光灯", true);
            return;
        }
        if (!isTorchOn) {
            return;
        }
        if (isLOLLIPOP()) {
            turnLightOffCamera2();
        } else {
            turnLightOffCamera(camera);
        }
        isTorchOn = false;
    }

    /**
     * 开启Camera2闪光灯
     */
    private void turnLightOnCamera2() {
        new Object() {
            private void _turnLightOnCamera2() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        MyApplication.INSTANCE.toast("应用未开启相机权限", true);
                        return;
                    }
                    try {
                        manager.openCamera(cameraId, new CameraDevice.StateCallback() {

                            @Override
                            public void onOpened(CameraDevice camera) {
                                cameraDevice = camera;
                                createCaptureSession();
                            }

                            @Override
                            public void onError(CameraDevice camera, int error) {
                            }

                            @Override
                            public void onDisconnected(CameraDevice camera) {
                            }
                        }, null);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                        MyApplication.INSTANCE.toast("开启失败", true);
                    }
                }
            }
        }._turnLightOnCamera2();
    }

    /**
     * 关闭Camera2闪光灯
     */
    private void turnLightOffCamera2() {
        new Object() {
            private void _turnLightOffCamera2() {
                if (cameraDevice != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cameraDevice.close();
                }
            }
        }._turnLightOffCamera2();
    }

    /**
     * 判断设备是否支持闪光灯
     *
     * @return boolean
     */
    public boolean isSupportFlash() {
        if (isLOLLIPOP()) { // 判断当前Android系统版本是否 >= 21, 分别处理
            return isSupportFlashCamera2;
        } else {
            PackageManager pm = getActivity().getPackageManager();
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            for (FeatureInfo f : features) {
                // 判断设备是否支持闪光灯
                if (PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) {
                    return true;
                }
            }
            // 判断是否支持闪光灯,方式二
            // Camera.Parameters parameters = camera.getParameters();
            // if (parameters == null) {
            // return false;
            // }
            // List<String> flashModes = parameters.getSupportedFlashModes();
            // if (flashModes == null) {
            // return false;
            // }
        }
        return false;
    }

    /**
     * 通过设置Camera打开闪光灯
     *
     * @param mCamera
     */
    public void turnLightOnCamera(Camera mCamera) {
        mCamera.startPreview();
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> flashModes = parameters.getSupportedFlashModes();
        String flashMode = parameters.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
            // 开启闪光灯
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            }
        }
        isTorchOn = true;
    }

    /**
     * 通过设置Camera关闭闪光灯
     *
     * @param mCamera
     */
    public void turnLightOffCamera(Camera mCamera) {
        mCamera.stopPreview();
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> flashModes = parameters.getSupportedFlashModes();
        String flashMode = parameters.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
            // 关闭闪光灯
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
            }
        }
        isTorchOn = false;
    }

    /**
     * createCaptureSession
     */
    private void createCaptureSession() {
        new Object() {
            private void _createCaptureSession() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    final CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {

                        public void onConfigured(CameraCaptureSession arg0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                captureSession = arg0;
                                CaptureRequest.Builder builder;
                                try {
                                    builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                    builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                                    builder.addTarget(surface);
                                    request = builder.build();
                                    captureSession.capture(request, null, null);
                                    isTorchOn = true;
                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage(), e);
                                    MyApplication.INSTANCE.toast("开启失败", true);
                                }
                            }
                        }

                        public void onConfigureFailed(CameraCaptureSession arg0) {
                        }
                    };
                    surfaceTexture = new SurfaceTexture(0, false);
                    surfaceTexture.setDefaultBufferSize(1280, 720);
                    surface = new Surface(surfaceTexture);
                    ArrayList localArrayList = new ArrayList(1);
                    localArrayList.add(surface);
                    try {
                        cameraDevice.createCaptureSession(localArrayList, stateCallback, null);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                        MyApplication.INSTANCE.toast("开启失败", true);
                    }
                }
            }
        }._createCaptureSession();
    }

    public void flashLightOnClick(View view) {
        //zxing扫码集成的手电筒
        if (isTorchOn) {
            barcodeView.setTorchOff();
            isTorchOn = false;
            return;
        }
        barcodeView.setTorchOn();
        isTorchOn = true;
    }

    public void flashLigtCustom(View view) {
        //扫码被暂停，手动开启手电筒
        if (isTorchOn) {
            turnOff();
            return;
        }
        turnOn();
    }

    public void scanCodeOnClick(View view) {
        ll_code.setVisibility(View.GONE);
        barcodeView.resume();
        barcodeView.setVisibility(View.VISIBLE);
    }

    public void inputOnClick(View view) {
        barcodeView.pause();
        barcodeView.setVisibility(View.GONE);
        ll_code.setVisibility(View.VISIBLE);
    }

    private void checkBlueTooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            MyApplication.INSTANCE.toast("设备不支持蓝牙", true);
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            //请求开启设备蓝牙
            Intent openBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(openBT, Constant.REQUEST_ENABLE_BT);
        } else {
            openBlueToothSuccess();
        }

    }

    //连接蓝牙设备
    private void openBlueToothSuccess() {
        Intent intent = new Intent();
        intent.putExtra("key", macAddressModel.getKey());
        intent.putExtra("macaddress", macAddressModel.getMac2());
        intent.putExtra("sn", sn);
        this.setResult(RESULT_OK, intent);
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            MyApplication.INSTANCE.toast("开启蓝牙失败", true);
            this.finish();
            return;
        }
        //蓝牙开启成功
        openBlueToothSuccess();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
        if (isTorchOn) {
            turnOff();
        }
    }

    private void getKeyByCode(String sn){
        apiService.getKeyByCode(sn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<BaseResponse<MacAddressModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull BaseResponse<MacAddressModel> macAddressModelBaseResponse) {
                        macAddressModelBaseResponse.getData().setMac2(macAddressModelBaseResponse.getData().getMac2().toUpperCase());
                        macAddressModel = macAddressModelBaseResponse.getData();
                        checkBlueTooth();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

}
