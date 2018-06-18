package com.gostreamyourself.android;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.pedro.encoder.input.video.CameraOpenException;

import com.pedro.rtplibrary.rtsp.RtspCamera2;
import com.pedro.rtplibrary.view.OpenGlView;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ConnectCheckerRtsp, View.OnClickListener, SurfaceHolder.Callback {
    @BindView(R.id.main_surfaceView) OpenGlView surfaceView;
    @BindView(R.id.main_recycler) RecyclerView messagesView;
    @BindView(R.id.main_startSwitch) Switch startSwitch;
    @BindView(R.id.main_cameraSwitch) Switch cameraSwitch;
    @BindView(R.id.main_viewerCount) TextView viewerCountTextView;

    private static final String TAG = MainActivity.class.getSimpleName();
    private Socket socket;
    private Boolean isConnected = true;
    private String username;
    private boolean typing = false;
    private Handler typingHandler = new Handler();
    private List<Message> messages = new ArrayList<Message>();
    private RecyclerView.Adapter messageAdapter;
    private String URL;
    private RtspCamera2 rtspCamera2;

    private static final int CAMERA_REQUEST_CODE = 1888;
    private static final int REQUEST_LOGIN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        messageAdapter = new MessageAdapter(getApplicationContext(), messages);

        try {
            String url = "http://back3ndb0is.herokuapp.com/chat/socket?";
            IO.Options options = new IO.Options();
            options.query = "stream=5b20e0d7e7179a589280ca7f";
            socket = IO.socket(url, options);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        connectSocket();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

        URL = "rtsp://145.49.53.161:80/live/stream";

        rtspCamera2 = new RtspCamera2(surfaceView, this);
        Log.i(TAG, "onCreate: BITRATE" + rtspCamera2.getBitrate());
        surfaceView.getHolder().addCallback(this);

        startSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Toast.makeText(MainActivity.this, "Turned stream on", Toast.LENGTH_SHORT).show();
                    if (!rtspCamera2.isStreaming()) {
                        if (rtspCamera2.isRecording() || rtspCamera2.prepareAudio()) {
                            rtspCamera2.prepareVideo(1920, 1080, 60, 1228800, false, 90);

                            Log.i("test", "onCheckedChanged: STARTING STREAM");
                            rtspCamera2.startStream(URL);
                        } else {
                            Toast.makeText(MainActivity.this, "Error preparing stream, This device cant do it",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Turned stream off", Toast.LENGTH_SHORT).show();

                    rtspCamera2.stopStream();
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

        MessageAdapter adapter = new MessageAdapter(this, messages);
        messagesView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        layoutManager.setStackFromEnd(true);
        messagesView.setLayoutManager(layoutManager);
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

    private void connectSocket() {
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.on("MESSAGE", onNewMessage);
        socket.on("VIEWERS", onViewersChange);
        //socket.on("typing", onTyping);
        //1socket.on("stop typing", onStopTyping);
        socket.connect();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isConnected) {
                        Log.i(TAG, "run: Connected");
                        Toast.makeText(MainActivity.this, R.string.connect, Toast.LENGTH_LONG).show();
                        isConnected = true;
                    }
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "disconnected");
                    isConnected = false;
                    Toast.makeText(MainActivity.this, R.string.disconnect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "Error connecting");
                    Toast.makeText(MainActivity.this, R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder stringBuilder = new StringBuilder();
                    JSONObject data = (JSONObject) args[0];
                    JSONObject user;
                    String message;
                    Log.i(TAG, "run: MESSAGEGET");
                    
                    try {
                        user = data.getJSONObject("User");
                        stringBuilder.append(user.getString("Name"));
                        stringBuilder.append(": ");
                        username = stringBuilder.toString();
                        message = data.getString("Content");
                        Log.i(TAG, "run: " + message);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    //removeTyping(username);
                    addMessage(username, message);
                }
            });
        }
    };

    private Emitter.Listener onViewersChange = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers = 0;

                    try {
                        //username = data.getString("username");
                        numUsers = data.getInt("5b20e0d7e7179a589280ca7f");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    //addLog(getResources().getString(R.string.message_user_joined, username));
                    viewerCountTextView.setText(String.valueOf(numUsers));
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;

                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    addTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;

                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    removeTyping(username);
                }
            });
        }
    };

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!typing) {
                return;
            }

            typing = false;
            socket.emit("stop typing");
        }
    };

    private void addLog(String message) {
        messages.add(new Message.Builder(Message.TYPE_LOG).message(message).build());
        messageAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void leave() {
        username = null;
        socket.disconnect();
        socket.connect();
        startSignIn();
    }

    private void addMessage(String username, String message) {
        messages.add(new Message.Builder(Message.TYPE_MESSAGE).username(username).message(message).build());
        messageAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void addTyping(String username) {
        messages.add(new Message.Builder(Message.TYPE_ACTION).username(username).build());
        messageAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void removeTyping(String username) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);

            if (message.getType() == Message.TYPE_ACTION && message.getUsername().equals(username)) {
                messages.remove(i);
                messageAdapter.notifyItemRemoved(i);
            }
        }
    }

    private void startSignIn() {
        username = null;
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    private void scrollToBottom() {
        messagesView.scrollToPosition(messageAdapter.getItemCount() - 1);
    }
}
