package com.example.android.camera_filter.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.android.camera_filter.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.pedro.library.AutoPermissions;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    Toolbar tb;
    long backBtnTime = 0;

    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    Preview preview;
    ProcessCameraProvider cameraProvider;
    CameraSelector cameraSelector;
    Camera camera;
    ImageButton btn_capture;
    ImageCapture imageCapture;

    ImageView iv_captured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tb = findViewById(R.id.tb);
        previewView = findViewById(R.id.previewView);
        btn_capture = findViewById(R.id.btn_capture);
        iv_captured = findViewById(R.id.iv_captured);

        //툴바를 액티비티의 앱바로 지정
        setSupportActionBar(tb);

        //카메라 미리보기 설정
        setCamera();
        //카메라 권한체크
        AutoPermissions.Companion.loadAllPermissions(this, 321);

        //사진찍기
        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture();

                previewView.setVisibility(View.GONE);
                iv_captured.setVisibility(View.VISIBLE);
            }
        });
    }

    void capture(){
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);

                iv_captured.setImageResource(R.drawable.ic_launcher_foreground);
            }
        });
    }

    // CameraProvider 사용 가능 여부 확인
    void setCamera(){
        //CameraProvider를 요청
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        //CameraProvider를 요청한 후, 뷰를 만들 때 초기화에 성공했는지 확인
        cameraProviderFuture.addListener(() -> {
            try{
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            }catch (ExecutionException | InterruptedException e) {

            }
        }, ContextCompat.getMainExecutor(this));
    }
    // 카메라 선택 및 수명 주기와 사용 사례 결합
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider){
        // 1. Preview를 만든다.
        preview = new Preview.Builder()
                .build();

        // 2. 원하는 카메라 LensFacing 옵션을 지정한다.
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // 3. 선택한 카메라와 사용 사례를 수명 주기에 결합한다.
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // 4. 사진을 찍기 위한 기본적인 컨트롤 제공
        imageCapture = new ImageCapture.Builder()
                .build();

        // 5. preview를 previewView에 연결한다.
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;

        if(previewView.getVisibility() == View.GONE && iv_captured.getVisibility() == View.VISIBLE) {
            previewView.setVisibility(View.VISIBLE);
            iv_captured.setVisibility(View.GONE);
        }else{
            //두번 눌러 뒤로가기 종료
            if(0 <= gapTime && 2000 >= gapTime){
                super.onBackPressed();
            }else{
                backBtnTime = curTime;
                Toast.makeText(this, "한 번 더 이전 버튼을 누르면 앱을 종료합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}