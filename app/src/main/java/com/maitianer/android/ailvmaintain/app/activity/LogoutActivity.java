package com.maitianer.android.ailvmaintain.app.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.guuguo.android.lib.app.LBaseActivitySupport;
import com.maitianer.android.ailvmaintain.R;
import com.maitianer.android.ailvmaintain.app.retrofit.ApiService;
import com.maitianer.android.ailvmaintain.app.retrofit.MyRetrofit;
import com.maitianer.android.ailvmaintain.entity.BaseResponse;
import com.maitianer.android.ailvmaintain.entity.MyApplication;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

/**
 * Created by czq on 2017/9/20.
 */

public class LogoutActivity extends LBaseActivitySupport {

    private TextView tv_username;
    private SharedPreferences sp;
    private Retrofit retrofit = MyRetrofit.getRetrofit(null);
    private ApiService apiService = retrofit.create(ApiService.class);
    private String token;

    public static void intentTo(Activity activity) {
        Intent intent = new Intent(activity, LogoutActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_logout;
    }

    @Nullable
    @Override
    public Toolbar getToolBar() {
        return (Toolbar) findViewById(R.id.id_tool_bar);
    }

    @NotNull
    @Override
    protected String getHeaderTitle() {
        return "设置";
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
        tv_username = (TextView) findViewById(R.id.tv_username);
        sp = getSharedPreferences("User", MODE_PRIVATE);
        if (sp != null) {
            String username = sp.getString("username", "");
            tv_username.setText(username);
        }
    }

    public void onLogoutClick(View view) {
        logout();
    }

    private void logout() {
        if (sp != null) {
            token = sp.getString("token", "");
        }
        if (!TextUtils.isEmpty(token)) {
            dialogLoadingShow("加载中", false, 0, null);
            apiService.logout(token)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<BaseResponse>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull BaseResponse baseResponse) {
                                dialogCompleteShow("登出成功", new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        if (sp != null) {
                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.remove("token");
                                            editor.remove("username");
                                            editor.commit();
                                        }
                                        LoginActivity.intentTo(LogoutActivity.this);
                                    }
                                }, 400);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            dialogDismiss();
                            dialogErrorShow("登录异常:" +e.getMessage(),null,400);
                        }
                    });
        } else {
            MyApplication.INSTANCE.toast("token为空", true);
        }
    }

}
