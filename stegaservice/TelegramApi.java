package com.example.stegaservice;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface TelegramApi {

    @Multipart
    @POST("sendPhoto")
    Call<Void> sendPhoto(
            @Part("chat_id") RequestBody chatId,
            @Part MultipartBody.Part photo
    );
}
