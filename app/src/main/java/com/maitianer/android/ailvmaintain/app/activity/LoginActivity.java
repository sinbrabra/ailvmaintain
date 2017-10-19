package com.maitianer.android.ailvmaintain.app.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.flyco.systembar.SystemBarHelper;
import com.guuguo.android.lib.app.LBaseActivitySupport;
import com.maitianer.android.ailvmaintain.R;
import com.maitianer.android.ailvmaintain.app.retrofit.ApiService;
import com.maitianer.android.ailvmaintain.app.retrofit.MyRetrofit;
import com.maitianer.android.ailvmaintain.entity.BaseResponse;
import com.maitianer.android.ailvmaintain.entity.LoginModel;
import com.maitianer.android.ailvmaintain.entity.MyApplication;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

/**
 * Created by czq on 2017/9/20.
 */

public class LoginActivity extends LBaseActivitySupport {
    private Retrofit retrofit = MyRetrofit.getRetrofit(null);
    private ApiService apiService = retrofit.create(ApiService.class);
    private EditText et_username;
    private EditText et_password;

    public static void intentTo(Activity activity){
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_login;
    }

    //沉浸式
    @Override
    protected void initStatusBar() {
        super.initStatusBar();
        SystemBarHelper.tintStatusBar(getActivity(), ContextCompat.getColor(getActivity(), R.color.gray09), 0);
    }

    @Override
    protected boolean isStatusBarTextDark() {
        return true;
    }

    @Override
    protected void initView() {
        SharedPreferences sp = getSharedPreferences("User", MODE_PRIVATE);
        if (sp != null) {
            String token = sp.getString("token", null);
            if (!TextUtils.isEmpty(token)) {
                MainActivity.intentTo(LoginActivity.this);
            }
        }
        super.initView();
        et_username = (EditText) findViewById(R.id.user_name);
        et_password = (EditText) findViewById(R.id.password);
    }

    public void onLoginClick(View view) {
        String username = et_username.getText().toString();
        String password = et_password.getText().toString();
        if (TextUtils.isEmpty(username)) {
            MyApplication.INSTANCE.toast("请输入用户名", true);
            return;
        } else if (TextUtils.isEmpty(password)) {
            MyApplication.INSTANCE.toast("请输入密码", true);
            return;
        }
        login(username, password);
    }

    private void login(final String username, String password) {
        dialogLoadingShow("加载中", false, 0, null);
        apiService.login(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<BaseResponse<LoginModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addApiCall(d);
                    }

                    @Override
                    public void onSuccess(@NonNull BaseResponse<LoginModel> loginModelBaseResponse) {
                        int code = loginModelBaseResponse.getCode();
                        String msg = loginModelBaseResponse.getMsg();
                        dialogDismiss();
                        if (code == 200) {
                            final String token = loginModelBaseResponse.getData().getToken();
                            if (!TextUtils.isEmpty(token)) {
                                dialogCompleteShow("登录成功", new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        SharedPreferences sp = getSharedPreferences("User", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putString("token", token);
                                        editor.putString("username", username);
                                        editor.apply();
                                        MainActivity.intentTo(LoginActivity.this);
                                    }
                                }, 400);
                            } else {
                                MyApplication.INSTANCE.toast("token为空", true);
                            }
                        } else {
                            MyApplication.INSTANCE.toast(msg, true);
                        }

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        dialogDismiss();
                        dialogErrorShow("登录异常" +e.getMessage(),null,400);
                    }
                });
    }

}
