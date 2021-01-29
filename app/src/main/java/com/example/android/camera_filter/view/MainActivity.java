package com.example.android.camera_filter.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.android.camera_filter.R;
import com.example.android.camera_filter.view.custom.CameraSurfaceView;
import com.pedro.library.AutoPermissions;

public class MainActivity extends AppCompatActivity {
    Toolbar tb;

    SurfaceView cameraSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //카메라뷰 추가
        FrameLayout container = findViewById(R.id.surfaceView);
        cameraSurfaceView = new CameraSurfaceView(this);
        container.addView(cameraSurfaceView);

        tb = findViewById(R.id.tb);

        //툴바를 액티비티의 앱바로 지정
        setSupportActionBar(tb);

        //카메라 권한체크
        AutoPermissions.Companion.loadAllPermissions(this, 321);
    }
}