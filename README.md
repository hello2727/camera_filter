개인 프로젝트
==============================
>실시간 카메라에 필터 적용/사진찍기

앱스토어 등록여부
-----------------
* 미등록 (학습목적 프로젝트)

개발환경
-----------------
* Android Java JetPack(CameraX)

결과물
-----------------
- 처음 앱 실행시 나타나는 권한 허용 선택창
<div>
  <img width="200" src="https://user-images.githubusercontent.com/43267195/108215998-4bb1fc00-7175-11eb-809e-8b74a41f5a51.PNG"> 
</div>

환경설정
-----------------
- build.gradle(module)
```
dependencies {
  ...
    //필요한 모든 권한체크
    implementation 'com.github.pedroSG94:AutoPermissions:1.0.3'

    //CameraX 사용하기
    // CameraX core library using the camera2 implementation
    def camerax_version = '1.1.0-alpha01'
    def camerax_version2 = "1.0.0-alpha17"
    implementation "androidx.camera:camera-core:${camerax_version}"
    implementation "androidx.camera:camera-camera2:${camerax_version}"
    // If you want to additionally use the CameraX Lifecycle library
    implementation "androidx.camera:camera-lifecycle:${camerax_version}"
    // If you want to additionally use the CameraX View class
    implementation "androidx.camera:camera-view:${camerax_version2}"
    // If you want to additionally use the CameraX Extensions library
    implementation "androidx.camera:camera-extensions:${camerax_version2}"

    // Glide dependencies
    implementation 'com.github.bumptech.glide:glide:4.11.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.11.0'
}
```
- build.gradle(Project)
```
allprojects {
        ...
        maven {
            url 'https://jitpack.io'
        }
}
```
- manifest
```
...
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<application
  android:usesCleartextTraffic="true"
  android:allowBackup="false"
  <meta-data
        android:name="com.naver.maps.map.CLIENT_ID"
        android:value="{값}"/>
</application>        
```
