package com.grupo12.sw2p1movil.datos;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("login")
    Call<JsonObject> login(@Body JsonObject body);

    @POST("register")
    Call<JsonObject> register(@Body JsonObject body);

    @GET("actions")
    Call<JsonArray> getActions(@Header("Authorization") String token);

    @GET("triggers")
    Call<JsonArray> getTriggers(@Header("Authorization") String token);

    @GET("devices")
    Call<JsonArray> getAllDevices(@Header("Authorization") String authHeader);

    @GET("user/devices")
    Call<JsonArray> getDevices(@Header("Authorization") String authHeader);

    @POST("devices")
    Call<JsonObject> createDevice(@Header("Authorization") String authHeader, @Body JsonObject nuevo);

    @POST("buttons")
    Call<JsonObject> sendButtonTrigger(@Header("Authorization") String token, @Body JsonObject data);

    @GET("processes")
    Call<JsonArray> getProcesses(@Header("Authorization") String token, @Query("device_id") int deviceId);

    @POST("processes")
    Call<JsonObject> createProcess(@Header("Authorization") String token, @Body JsonObject data);

    @DELETE("processes/{id}")
    Call<Void> deleteProcess(@Header("Authorization") String token, @Path("id") int id);

    @POST("/api/buttons")
    Call<JsonObject> createButton(@Header("Authorization") String token, @Body JsonObject data);

    @GET("/api/buttons")
    Call<List<JsonObject>> getButtons(@Header("Authorization") String token);

    @DELETE("/api/buttons")
    Call<Void> deleteButton(@Header("Authorization") String token, @Query("device_id") int deviceId, @Query("button_id") String buttonId);

}
