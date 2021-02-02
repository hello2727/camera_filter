package com.example.android.camera_filter.view;

import java.nio.FloatBuffer;

public class Square {
    static final float vertices[] = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
    };
    static final float textureVertices[] = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };

    FloatBuffer verticesBuffer;
    FloatBuffer textureBuffer;

    int vertexShader = 0;
    int fragmentShader = 0;
    int program = 0;

    final String vertexShaderCode = "attribute vec4 aPosition;" +
            "attribute vec2 aTexPosition;" +
            "varying vec2 vTexPosition;" +
            "void main() {" +
            "  gl_Position = aPosition;" +
            "  vTexPosition = aTexPosition;" +
            "}";

    final String fragmentShaderCode = "precision mediump float;" +
            "uniform sampler2D uTexture;" +
            "varying vec2 vTexPosition;" +
            "void main() {" +
            "  gl_FragColor = texture2D(uTexture, vTexPosition);" +
            "}";

    public Square() {
        initializeBuffers();
        initializeProgram();
    }

    void initializeBuffers(){

    }

    void initializeProgram(){

    }

    void draw(int texture){
        
    }
}
