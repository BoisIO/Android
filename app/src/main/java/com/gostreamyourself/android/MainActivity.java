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
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.pedro.encoder.input.video.CameraOpenException;

import com.pedro.rtplibrary.rtsp.RtspCamera2;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ConnectCheckerRtsp, View.OnClickListener, SurfaceHolder.Callback {
    @BindView(R.id.main_surfaceView) SurfaceView surfaceView;
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
            socket = IO.socket("http://noauthy.herokuapp.com/chat/5b2235eae7179a589281bbf5");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        connectSocket();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

        URL = "rtsp://145.49.24.137/live/stream";

        rtspCamera2 = new RtspCamera2(surfaceView, this);

        surfaceView.getHolder().addCallback(this);
        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            int Low = 10;
            int High = 5000;
            int Result = r.nextInt(High-Low) + Low;
            Message test = new Message.Builder(Message.TYPE_MESSAGE).message(("Message: " + i)).username("user" + Result + ": ").build();

            if(i == 43){
                test = new Message.Builder(Message.TYPE_MESSAGE).message("DIT IS EEN FUCKING LANG BERICHT OM TE TESTEN OF DE APPLICATIE DIT ONDERSTEUNT").username("user" + Result + ": ").build();
            }

            Log.i(TAG, "onCreate: Created Message: " + i);
            messages.add(test);
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

        MessageAdapter adapter = new MessageAdapter(this, messages);
        messagesView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        //layoutManager.setReverseLayout(true);
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

    private void connectSocket() {
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.on("new message", onNewMessage);
        socket.on("user joined", onUserJoined);
        socket.on("user left", onUserLeft);
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
                        Toast.makeText(getApplicationContext(), R.string.connect, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), R.string.disconnect, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), R.string.error_connect, Toast.LENGTH_LONG).show();
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
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;

                    try {
                        username = data.getString("username");
                        message = data.getString("message");
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

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;

                    try {
                        //username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    //addLog(getResources().getString(R.string.message_user_joined, username));
                    viewerCountTextView.setText(numUsers);
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;

                    try {
                        //username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    //addLog(getResources().getString(R.string.message_user_left, username));
                    viewerCountTextView.setText(numUsers);
                    //removeTyping(username);
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
