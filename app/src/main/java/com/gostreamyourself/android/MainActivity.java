package com.gostreamyourself.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.pedro.rtplibrary.rtmp.RtmpCamera2;
import com.pedro.rtplibrary.rtsp.RtspCamera1;
import com.pedro.rtplibrary.rtsp.RtspCamera2;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;

import net.ossrs.rtmp.ConnectCheckerRtmp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ConnectCheckerRtsp, View.OnClickListener, SurfaceHolder.Callback{
    @BindView(R.id.main_surfaceView) SurfaceView surfaceView;
    @BindView(R.id.main_recycler) RecyclerView recyclerView;
    @BindView(R.id.main_startSwitch) Switch startSwitch;
    @BindView(R.id.main_cameraSwitch) Switch cameraSwitch;
    @BindView(R.id.testButton) Button testButton;

    private CameraManager cameraManager;
    private int cameraFacing;
    private TextureView.SurfaceTextureListener textureListener;
    private CameraDevice cameraDevice;

    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest captureRequest;


    private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/rtmp-rtsp-stream-client-java");

    private CameraDevice.StateCallback stateCallback;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    private String URL;

    private RtspCamera2 rtspCamera2;

    private String cameraID;
    Size previewSize;

    private static final int CAMERA_REQUEST_CODE = 1888;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

        URL = "rtsp://145.49.24.137/live/stream";

        rtspCamera2 = new RtspCamera2(surfaceView, this);

        surfaceView.getHolder().addCallback(this);

        final ArrayList<Message> msgs = new ArrayList<Message>();

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message test = new Message();
                test.setMessage("Dit is een bericht dat door een random gebruiker is gestuurd en deze wordt in de applicatie getoond.");
                msgs.add(test);

                recyclerView.getAdapter().notifyDataSetChanged();
                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);

            }
        });

        for (int i = 0; i < 100; i++) {
            Message test = new Message();
            test.setMessage("Message: " + i);
            if(i == 43){
                test.setMessage("DIT IS EEN FUCKING LANG BERICHT OM TE TESTEN OF DE APPLICATIE DIT ONDERSTEUNT");
            }
            Log.i("YOLO", "onCreate: Created Message: " + i);
            msgs.add(test);
        }

        startSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Toast.makeText(MainActivity.this, "Turned stream on", Toast.LENGTH_SHORT).show();
                    if (!rtspCamera2.isStreaming()) {
                        if (rtspCamera2.isRecording() || rtspCamera2.prepareAudio() && rtspCamera2.prepareVideo()) {

                            Log.i("test", "onCheckedChanged: STARTING STREAM");
                            rtspCamera2.startStream(URL);
                        } else {
                            Toast.makeText(MainActivity.this, "Error preparing stream, This device cant do it",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Turned stream off", Toast.LENGTH_SHORT).show();

                        rtspCamera2.stopStream();
                    }
                }
            }
        });



        cameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    try {
                        rtspCamera2.switchCamera();
                    } catch (CameraOpenException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
            }
        });

        MessageListAdapter adapter = new MessageListAdapter(this, msgs);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        //layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

    }


    @Override
    public void onConnectionSuccessRtsp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onConnectionFailedRtsp(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Connection failed. " + reason, Toast.LENGTH_LONG)
                        .show();
                rtspCamera2.stopStream();
                //button.setText(R.string.start_button);
            }
        });
    }

    @Override
    public void onDisconnectRtsp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthErrorRtsp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
                rtspCamera2.stopStream();
                //button.setText(R.string.start_button);
            }
        });
    }

    @Override
    public void onAuthSuccessRtsp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        rtspCamera2.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        rtspCamera2.stopPreview();
    }


    @Override
    public void onClick(View view) {

    }

    //@Override
    //public void onConnectionSuccessRtmp() {
    //    runOnUiThread(new Runnable() {
    //        @Override
    //        public void run() {
    //            Toast.makeText(MainActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
    //        }
    //    });
    //}
//
    //@Override
    //public void onConnectionFailedRtmp(final String reason) {
    //    runOnUiThread(new Runnable() {
    //        @Override
    //        public void run() {
    //            Toast.makeText(MainActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
    //                    .show();
    //            rtspCamera2.stopStream();
    //        }
    //    });
    //}
//
    //@Override
    //public void onDisconnectRtmp() {
    //    runOnUiThread(new Runnable() {
    //        @Override
    //        public void run() {
    //            Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
    //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    //                    && rtspCamera2.isRecording()) {
    //                rtspCamera2.stopRecord();
    //            }
    //        }
    //    });
    //}
//
    //@Override
    //public void onAuthErrorRtmp() {
    //    runOnUiThread(new Runnable() {
    //        @Override
    //        public void run() {
    //            Toast.makeText(MainActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
    //        }
    //    });
    //}
//
    //@Override
    //public void onAuthSuccessRtmp() {
    //    runOnUiThread(new Runnable() {
    //        @Override
    //        public void run() {
    //            Toast.makeText(MainActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
    //        }
    //    });
    //}

}
