package com.example.android.camera_filter.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.os.Bundle;

import com.example.android.camera_filter.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.pedro.library.AutoPermissions;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    Toolbar tb;

    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tb = findViewById(R.id.tb);
        previewView = findViewById(R.id.previewView);

        //툴바를 액티비티의 앱바로 지정
        setSupportActionBar(tb);

        //카메라뷰 추가
        setCamera();
        //카메라 권한체크
        AutoPermissions.Companion.loadAllPermissions(this, 321);
    }

    void setCamera(){
        //CameraProvider를 요청
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        //CameraProvider를 요청한 후, 뷰를 만들 때 초기화에 성공했는지 확인
        cameraProviderFuture.addListener(() -> {
            try{
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            }catch (ExecutionException | InterruptedException e) {

            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider){
        // 1. Preview를 만든다.
        Preview preview = new Preview.Builder()
                .build();

        // 2. 원하는 카메라 LensFacing 옵션을 지정한다.
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // 3. 선택한 카메라와 사용 사례를 수명 주기에 결합한다.
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // 4. preview를 previewView에 연결한다.
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
    }
}