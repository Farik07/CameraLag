package com.nightmaredevs.cameralag;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteBindOrColumnIndexOutOfRangeException;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private TextureView textureView1;
    private CameraDevice cameraDevice;
    private String cameraId;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;

    Handler backgroudHandler;
    HandlerThread handlerThread;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // En esta linea se declara que la orientacion sera landscape
        hideNavigation();
        verifyPermission();
        textureView1 = (findViewById(R.id.textureView1));
        textureView1 = (findViewById(R.id.textureView2));
        textureView1.setSurfaceTextureListener(surfaceTextureListener);
        //textureView2.setSurfaceTextureListener(surfaceTextureListener);

    }

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void openCamera() throws CameraAccessException{
        CameraManager cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        cameraId = cameraManager.getCameraIdList()[0];
        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        imageDimension = map.getOutputSizes( SurfaceTexture.class)[0];
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            return;
        }
        cameraManager.openCamera(cameraId,stateCallback,null);
    }

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                startCamPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void startCamPreview() throws CameraAccessException {
        SurfaceTexture texture = textureView1.getSurfaceTexture();
        //SurfaceTexture texture1 =textureView2.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
        //texture1.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
        Surface surface = new Surface(texture);
        //Surface surface1 = new Surface(texture1);
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);
       // captureRequestBuilder.addTarget(surface1);
        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                if (cameraDevice == null){
                    return;
                }
                cameraCaptureSessions = session;
                try {
                    updateCamPreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        },null);

    }

    private void updateCamPreview() throws CameraAccessException {
        if(cameraDevice == null){
            return;
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, backgroudHandler);
    }

    @Override
    protected void onResume(){
        super.onResume();
        startBackgroudThread();
        if (textureView1.isAvailable()){
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        else {
            textureView1.setSurfaceTextureListener(surfaceTextureListener) ;
            //textureView2.setSurfaceTextureListener(surfaceTextureListener) ;
        }
    }

    private void startBackgroudThread() {
        handlerThread = new HandlerThread("Camera backgroud");
        handlerThread.start();
        backgroudHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    protected void onPause() {
        try {
            stopBackgroudThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    private void stopBackgroudThread() throws InterruptedException {
        handlerThread.quitSafely();
        handlerThread.join();

        backgroudHandler = null;
        handlerThread = null;

    }

    public void hideNavigation(){
        this.getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
    }

    private void verifyPermission(){
        String [] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[0])!= PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[1])!= PackageManager.PERMISSION_GRANTED ) {

                    ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
    }


}
