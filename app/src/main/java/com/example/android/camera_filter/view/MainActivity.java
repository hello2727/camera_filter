package com.example.android.camera_filter.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.android.camera_filter.R;
import com.example.android.camera_filter.util.YuvToRgbConverter;
import com.example.android.camera_filter.view.filter.MyGLRenderer;
import com.google.common.util.concurrent.ListenableFuture;
import com.pedro.library.AutoPermissions;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    Toolbar tb;
    long backBtnTime = 0;

    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    Preview preview;
    PreviewView previewView;
    GLSurfaceView glSurfaceView;
    MyGLRenderer renderer;
    final ExecutorService executors = Executors.newSingleThreadExecutor();
    ProcessCameraProvider cameraProvider;
    CameraSelector cameraSelector;
    ImageAnalysis imageAnalysis;
    Camera camera;
    ImageButton btn_capture, btn_filterSelection;
    ImageCapture imageCapture;
    FrameLayout captured_picture;
    YuvToRgbConverter converter;

    ImageView iv_captured;
    ScrollView sv_filter;
    boolean isUp = false;
    TextView filter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tb = findViewById(R.id.tb);
        previewView = findViewById(R.id.previewView);
        converter = new YuvToRgbConverter(this);
        glSurfaceView = findViewById(R.id.glSurfaceView);
        renderer = new MyGLRenderer(glSurfaceView, converter, this);
        btn_capture = findViewById(R.id.btn_capture);
        iv_captured = findViewById(R.id.iv_captured);
        btn_filterSelection = findViewById(R.id.btn_filterSelection);
        sv_filter = findViewById(R.id.sv_filter);
        filter1 = findViewById(R.id.filter1);
        captured_picture = findViewById(R.id.captured_picture);

        //툴바를 액티비티의 앱바로 지정
        setSupportActionBar(tb);

        //카메라 필터 적용
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT); //투명
        //GLSurfaceView 가 Camera preview 보다 상위에서 그려지도록 한다.
        glSurfaceView.setZOrderOnTop(true);
        glSurfaceView.setRenderer(renderer);
        //Surface가 생성될때와 GLSurfaceView클래스의 requestRender 메소드가 호출될때에만
        //화면을 다시 그리게 됩니다
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        //카메라 권한체크
        AutoPermissions.Companion.loadAllPermissions(this, 321);
        //카메라 미리보기 설정
        setupCamera();

        //사진찍기
        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture();
            }
        });

        //카메라 필터 고르기
        btn_filterSelection.setOnClickListener(v -> {
            Animation translateUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translate_up);
            sv_filter.setVisibility(View.VISIBLE);
            sv_filter.startAnimation(translateUp);
            isUp = true;
        });

        //카메라에 필터1 입히기
        filter1.setOnClickListener(v -> {
            if(glSurfaceView.getVisibility() == View.INVISIBLE){
                glSurfaceView.setVisibility(View.VISIBLE);
            }else{
                glSurfaceView.setVisibility(View.INVISIBLE);
            }
        });
    }

    void capture(){
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                super.onCaptureSuccess(imageProxy);

                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                Matrix roatateMatrix = new Matrix();
                roatateMatrix.postRotate(rotationDegrees);

                //비트맵으로 변환 후 회전
                Bitmap originImg = imageProxyToBitmap(imageProxy);
                Bitmap rotateImg = Bitmap.createBitmap(originImg, 0, 0, originImg.getWidth(), originImg.getHeight(), roatateMatrix, true);

                Glide.with(getApplicationContext())
                        .load(rotateImg)
                        .into(iv_captured);

                previewView.setVisibility(View.GONE);
                iv_captured.setVisibility(View.VISIBLE);

                imageProxy.close();
            }
        });
    }

    //이미지프록시를 비트맵으로 변환
    Bitmap imageProxyToBitmap(ImageProxy imageProxy){
        ByteBuffer byteBuffer = imageProxy.getPlanes()[0].getBuffer();
        byteBuffer.rewind();
        byte[] bytes = new byte[byteBuffer.capacity()];
        byteBuffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);
    }

    // CameraProvider 사용 가능 여부 확인
    void setupCamera(){
        //CameraProvider를 요청
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        //CameraProvider를 요청한 후, 뷰를 만들 때 초기화에 성공했는지 확인
        cameraProviderFuture.addListener(() -> {
            try{
                cameraProvider = cameraProviderFuture.get();
                glSurfaceView.post(new Runnable() {
                    @Override
                    public void run() {
                        bindPreview(cameraProvider);
                    }
                });
            }catch (ExecutionException | InterruptedException e) {

            }
        }, ContextCompat.getMainExecutor(this));
    }
    // 카메라 선택 및 수명 주기와 사용 사례 결합
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider){
        // Preview를 만든다.
        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();
        // 선택한 카메라와 사용 사례를 수명 주기에 결합한다.
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // 원하는 카메라 LensFacing 옵션을 지정한다.
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // 사진을 찍기 위한 기본적인 컨트롤 제공
        imageCapture = new ImageCapture.Builder().build();

        //이미지 분석(이미지 처리, 컴퓨터 비전 또는 머신러닝 추론을 진행할 수 있도록 CPU에서 액세스 가능한 이미지를 앱에 제공)
        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(executors, renderer);

        //회전(얼굴 인식에서 얼굴의 방향을 올바르게 감지하거나 사진이 가로 또는 세로 모드로 설정되도록 하기 위해 회전 인식이 필요할 수 있음)
        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation;

                if(orientation >= 45 && orientation < 135){
                    rotation = Surface.ROTATION_270;
                }else if(orientation >= 135 && orientation < 225){
                    rotation = Surface.ROTATION_180;
                }else if(orientation >= 225 && orientation < 315){
                    rotation = Surface.ROTATION_90;
                }else{
                    rotation = Surface.ROTATION_0;
                }

                imageCapture.setTargetRotation(rotation);
            }
        };
        orientationEventListener.enable();

        // preview를 previewView에 연결한다.
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;

        if(isUp) {
            Animation translateDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translate_down);
            sv_filter.setVisibility(View.GONE);
            sv_filter.startAnimation(translateDown);
            isUp = false;
        }else if(previewView.getVisibility() == View.GONE && iv_captured.getVisibility() == View.VISIBLE){
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