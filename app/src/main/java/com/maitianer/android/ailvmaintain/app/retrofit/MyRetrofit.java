package com.maitianer.android.ailvmaintain.app.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.maitianer.android.ailvmaintain.entity.Constant;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by czq on 2017/9/21.
 */

public class MyRetrofit {
    private static String pattern = "yyyy-MM-dd HH:mm:ss";

    public static GsonConverterFactory getGsonConver(GsonBuilder gsonBuilder) {
        Gson gson = new GsonBuilder().setDateFormat(pattern).create();
        if (gsonBuilder != null) {
            gson = gsonBuilder.setDateFormat(pattern).create();
        }
        return GsonConverterFactory.create(gson);
    }

    static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    return chain.proceed(request);
                }
            })
            .build();

    public static Retrofit getRetrofit(GsonBuilder gsonBuilder) {
        return new Retrofit.Builder()
                .baseUrl(Constant.BASE_URL)
                .client(client)
                .addConverterFactory(getGsonConver(gsonBuilder))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }
}
