package com.example.shipra.mobileapplication.Utils;

import com.example.shipra.mobileapplication.Retrofit.mobileAppAPI;
import com.example.shipra.mobileapplication.Retrofit.RetrofitClient;



 public class Common {

 private static final String BASE_URL="http://localhost/mobileapp/";

 public static mobileAppAPI getAPI(){

    return RetrofitClient.getClient(BASE_URL).create(mobileAppAPI.class);

}

}
