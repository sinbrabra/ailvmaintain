package com.maitianer.android.ailvmaintain.app.retrofit;

import com.maitianer.android.ailvmaintain.entity.BaseResponse;
import com.maitianer.android.ailvmaintain.entity.CarInformation;
import com.maitianer.android.ailvmaintain.entity.LoginModel;
import com.maitianer.android.ailvmaintain.entity.MacAddressModel;
import com.maitianer.android.ailvmaintain.entity.NearCarModel;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by czq on 2017/9/21.
 */

public interface ApiService {

    //获取距离最近的50辆车
    @GET("repair/car/getNearCar")
    Single<BaseResponse<List<NearCarModel>>> getNearCars(@Query("lat") double lat, @Query("lng") double lng, @Query("search_type") Integer search_type);

    //登录
    @FormUrlEncoded
    @POST("repair/login/loginAction")
    Single<BaseResponse<LoginModel>> login(@Field("username") String username, @Field("password") String password);

    //登出
    @FormUrlEncoded
    @POST("repair/login/logout")
    Single<BaseResponse> logout(@Field("token") String token);

    //根据id获取车辆信息
    @GET("repair/car/getCarByID")
    Single<BaseResponse<NearCarModel>> getCarById(@Query("id")Integer id);

    //设为正常
    @FormUrlEncoded
    @POST("repair/car/setNomal")
    Single<BaseResponse> setNormal(@Field("id") Integer id, @Field("token") String token);

    //设为故障
    @FormUrlEncoded
    @POST("repair/car/setError")
    @Headers("Content-Type: application/x-www-form-urlencoded;charset=UTF-8")
    Single<BaseResponse> setError(@Field("id") Integer id, @Field("token") String token, @Field(value = "repair_content", encoded = true) String content);

    //获取故障信息
    @GET("repair/car/getRepairContent")
    Single<BaseResponse<CarInformation>> getCarInformation(@Query("id")Integer id);

    //获取鉴权码
    @GET("repair/car/getKeyByCode")
    Single<BaseResponse<MacAddressModel>> getKeyByCode(@Query("sn") String sn);
}
