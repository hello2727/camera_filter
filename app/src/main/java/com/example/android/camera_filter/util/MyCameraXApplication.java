package com.example.android.camera_filter.util;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;

/* CameraX가 초기화되는 시점을 세밀하게 제어. */

public class MyCameraXApplication extends Application implements CameraXConfig.Provider {

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }
}
