package com.example.android.camera_filter.view.filter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import java.nio.ByteBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer, ImageAnalysis.Analyzer {
    GLSurfaceView mGlSurfaceView;

    private int textures[] = new int[2];
    private Square square;
    Bitmap photo;
    EffectContext effectContext;
    Effect effect;

    public MyGLRenderer(GLSurfaceView glSurfaceView) {
        super();
        this.mGlSurfaceView = glSurfaceView;
    }

    //GLSurfaceView가 생성되었을때 한번 호출되는 메소드입니다.
    //OpenGL 환경 설정, OpenGL 그래픽 객체 초기화 등과 같은 처리를 할때 사용됩니다
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //color buffer를 클리어할 때 사용할 색을 지정합니다.
        //red, green, blue, alpha 순으로 0~1사이의 값을 지정합니다.
//        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 0.0f);
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
        GLES20.glClearColor(0.5f, 0.6f, 0.7f, 0.5f);
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
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT); //**중요

        generateSquare();

        if(effectContext == null){
            effectContext = EffectContext.createWithCurrentGlContext();
        }

        if(effect != null){
            effect.release();
        }

        grayScaleEffect();

        if(square != null){
//            square.draw(textures[1]);
        }
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
        square = new Square();
    }

    void grayScaleEffect(){
        EffectContext effectContext = this.effectContext;

        EffectFactory factory = effectContext.getFactory();
        effect = factory.createEffect(EffectFactory.EFFECT_GRAYSCALE);

        if(photo != null && effectContext != null){
            effect.apply(textures[0], photo.getWidth(), photo.getHeight(), textures[1]);
        }
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        int rotationDegrees = image.getImageInfo().getRotationDegrees();

        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);

        Bitmap b = imageProxyToBitmap(image);
        if(b == null){
            return;
        }
        Bitmap bm = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
        setImage(bm);

        mGlSurfaceView.requestRender();
    }

    public void setImage(Bitmap image){
        if(photo != null){
            this.photo.recycle();
            this.photo = image;
        }
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
