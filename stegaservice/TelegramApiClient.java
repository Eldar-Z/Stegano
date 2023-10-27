package com.example.stegaservice;

import android.util.Log;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

import java.io.File;

public class TelegramApiClient {

    private static final String BASE_URL = "https://api.telegram.org/bot6621783642:AAFOZ9HC4F6dNijqbaUMX9F_Q6abJB5aYsQ/";

    private static TelegramApi telegramApi;

    public static void initTelegramApi() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        telegramApi = retrofit.create(TelegramApi.class);
    }

    public TelegramApiClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        telegramApi = retrofit.create(TelegramApi.class);
    }

    public static void sendPhoto(String chatId, File photoFile) {
        RequestBody chatIdRequestBody = RequestBody.create(MediaType.parse("text/plain"), chatId);
        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("image/*"), photoFile);
        MultipartBody.Part photoPart = MultipartBody.Part.createFormData("photo", photoFile.getName(), photoRequestBody);

        Call<Void> call = telegramApi.sendPhoto(chatIdRequestBody, photoPart);
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("TelegramApiClient", "Photo sent successfully");
                } else {
                    Log.e("TelegramApiClient", "Failed to send photo. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("TelegramApiClient", "Failed to send photo", t);
            }
        });
    }
}

