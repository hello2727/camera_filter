package com.example.android.camera_filter.view.filter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.android.camera_filter.R;
import com.example.android.camera_filter.util.YuvToRgbConverter;

import java.nio.ByteBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer, ImageAnalysis.Analyzer {
    GLSurfaceView mGlSurfaceView;
    YuvToRgbConverter converter;

    int WIDTH = 1100;
    int HEIGHT = 1500;
    private int textures[] = new int[2];
    private Square mSquare;
    private Bitmap photo;
    private int photoWidth, photoHeight;
    EffectContext effectContext;
    Effect effect;

    public MyGLRenderer(GLSurfaceView glSurfaceView, YuvToRgbConverter converter, Context context) {
        super();
        this.mGlSurfaceView = glSurfaceView;
        this.converter = converter;
        //photo = BitmapFactory.decodeResource(context.getResources(), R.drawable.p);
        //photoWidth = photo.getWidth();
        //photoHeight = photo.getHeight();
    }

    //GLSurfaceView가 생성되었을때 한번 호출되는 메소드입니다.
    //OpenGL 환경 설정, OpenGL 그래픽 객체 초기화 등과 같은 처리를 할때 사용됩니다
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //shape가 정의된 Triangle 클래스의 인스턴스를 생성합니다.
        mSquare = new Square();

        //color buffer를 클리어할 때 사용할 색을 지정합니다.
        //red, green, blue, alpha 순으로 0~1사이의 값을 지정합니다.
//        GLES20.glClearColor(0.5f, 0.6f, 0.7f, 0.5f);
    }

    //GLSurfaceView의 크기 변경 또는 디바이스 화면의 방향 전환 등으로 인해
    //GLSurfaceView의 geometry가 바뀔때 호출되는 메소드입니다.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //viewport를 설정합니다.
        //specifies the affine transformation of x and y from
        //normalized device coordinates to window coordinates
        //viewport rectangle의 왼쪽 아래를 (0,0)으로 지정하고
        //viewport의 width와 height를 지정합니다.
        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(0.621f, 0.453f, 0.3f, 0.5f);
        generateSquare();
    }

    //GLSurfaceView가 다시 그려질때 마다 호출되는 메소드입니다.
    @Override
    public void onDrawFrame(GL10 gl) {
        //glClearColor에서 설정한 값으로 color buffer를 클리어합니다.
        //glClear메소드를 사용하여 클리어할 수 있는 버퍼는 다음 3가지 입니다.
        //Color buffer (GL_COLOR_BUFFER_BIT)
        //depth buffer (GL_DEPTH_BUFFER_BIT)
        //stencil buffer (GL_STENCIL_BUFFER_BIT)i4rh
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT); //화면을 깨끗하게!

        //generateSquare();

        if(effectContext == null){
            effectContext = EffectContext.createWithCurrentGlContext();
        }

        if(effect != null){
            effect.release();
        }

        grayScaleEffect();

        if(mSquare != null){
            mSquare.draw(textures[1]);
        }
    }

    public static int loadShader(int type, String shaderCode){
        // 다음 2가지 타입 중 하나로 shader객체를 생성한다.
        // vertex shader type (GLES20.GL_VERTEX_SHADER)
        // 또는 fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // shader객체에 shader source code를 로드합니다.
        GLES20.glShaderSource(shader, shaderCode);

        //shader객체를 컴파일 합니다.
        GLES20.glCompileShader(shader);

        return shader;
    }

    void generateSquare(){
        GLES20.glGenTextures(2, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        if(photo != null){
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, photo, 0);
        }
        mSquare = new Square();
    }

    void grayScaleEffect(){
        EffectContext effectContext = this.effectContext;

        EffectFactory factory = effectContext.getFactory();
        effect = factory.createEffect(EffectFactory.EFFECT_GRAYSCALE);

        effect.apply(textures[0], WIDTH, HEIGHT, textures[1]);
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    @Override
    public void analyze(@NonNull ImageProxy image) {
        int degree = image.getImageInfo().getRotationDegrees();

        //Bitmap b = allocateBitmapIfNecessary(image.getWidth(), image.getHeight());
        //converter.yuvToRgb(image.getImage(), photo);
        //image.close();
        //if(b == null){
        //    return;
        //}

        //Matrix matrix = new Matrix();
        //matrix.postRotate(90);
        //Bitmap bm = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
        //setImage(bm);

        //mGlSurfaceView.requestRender();
    }

    public synchronized void setImage(Bitmap image){
        if(photo != null){
            this.photo.recycle();
            this.photo = image;
            photoWidth = image.getWidth();
            photoHeight =image.getHeight();
        }
    }

    Bitmap allocateBitmapIfNecessary(int width, int height){
        if(photo == null || photo.getWidth() != width || photo.getHeight() != height){
            photo = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        return photo;
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
}
