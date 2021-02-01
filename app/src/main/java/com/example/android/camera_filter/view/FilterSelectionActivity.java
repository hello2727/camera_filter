package com.example.android.camera_filter.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import com.example.android.camera_filter.R;

import java.util.Random;

public class FilterSelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_selection);

        View view = getWindow().getDecorView().getRootView();

        WindowManager.LayoutParams params = (WindowManager.LayoutParams)view.getLayoutParams();
        if(params == null){
            return;
        }
        params.height = 200 + 200*new Random().nextInt(100) / 100;
        params.gravity = Gravity.TOP;

        ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(view, params);
    }
}