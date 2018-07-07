package com.example.shipra.mobileapplication.Retrofit;


import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import com.example.shipra.mobileapplication.model.CheckUserResponse;
import com.example.shipra.mobileapplication.model.User;

public interface mobileAppAPI {


   @FormUrlEncoded
    @POST("checkuser.php")
    Call<CheckUserResponse> checkUserExists(@Field("phone")String phone);


    @FormUrlEncoded
    @POST("register.php")
    Call<User> registerNewUser(@Field("name")String name,
                               @Field("phone")String phone,
                               @Field("email")String email);
}
